package co.com.bookhive.gateway.infrastructure.output.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import co.com.bookhive.gateway.testdata.MutableClock;
import co.com.bookhive.gateway.testdata.SessionTokens;
import co.com.bookhive.gateway.testdata.StubHttp;

class MetadataIdentityTokensTest {

	private static final String BACKEND = "https://bookhive-api-164070755294.us-east1.run.app";

	private final MutableClock clock = new MutableClock(SessionTokens.NOW);

	private static String tokenExpiringIn(final Duration lifetime) {
		return SessionTokens.signed(SessionTokens.validClaims()
				.expirationTime(Date.from(SessionTokens.NOW.plus(lifetime)))
				.build());
	}

	@Test
	@DisplayName("pide la identidad al servidor de metadatos con la audiencia del backend y la reutiliza")
	void asksMetadataServerAndReusesToken() {
		final String token = tokenExpiringIn(Duration.ofHours(1));
		final StubHttp http = StubHttp.answering(HttpStatus.OK, MediaType.TEXT_PLAIN, token + "\n");
		final MetadataIdentityTokens tokens = new MetadataIdentityTokens(http.builder(), clock);

		assertEquals(token, tokens.tokenFor(BACKEND).block());
		assertEquals(token, tokens.tokenFor(BACKEND).block());

		assertEquals(1, http.requests().size());
		assertEquals("Google", http.requests().get(0).headers().getFirst(MetadataIdentityTokens.METADATA_FLAVOR));
		assertEquals("audience=" + BACKEND, http.requests().get(0).url().getQuery());
	}

	@Test
	@DisplayName("la renueva cinco minutos antes de que venza")
	void renewsBeforeExpiry() {
		final StubHttp http = StubHttp.answering(HttpStatus.OK, MediaType.TEXT_PLAIN, tokenExpiringIn(Duration.ofHours(1)));
		final MetadataIdentityTokens tokens = new MetadataIdentityTokens(http.builder(), clock);

		tokens.tokenFor(BACKEND).block();
		clock.advance(Duration.ofMinutes(56));
		tokens.tokenFor(BACKEND).block();

		assertEquals(2, http.requests().size());
	}

	@Test
	@DisplayName("si la respuesta no es un pase, la guarda solo unos minutos")
	void unreadableTokenGetsShortLife() {
		assertEquals(SessionTokens.NOW.plus(MetadataIdentityTokens.FALLBACK_LIFETIME),
				MetadataIdentityTokens.renewAt("no-es-un-pase", SessionTokens.NOW));
		assertEquals(SessionTokens.NOW.plus(MetadataIdentityTokens.FALLBACK_LIFETIME),
				MetadataIdentityTokens.renewAt(SessionTokens.signed(SessionTokens.validClaims().expirationTime(null).build()),
						SessionTokens.NOW));
	}

	@Test
	@DisplayName("una respuesta vacía no deja pase")
	void emptyAnswerGivesNothing() {
		final StubHttp http = StubHttp.answering(HttpStatus.OK, MediaType.TEXT_PLAIN, "   ");
		final MetadataIdentityTokens tokens = new MetadataIdentityTokens(http.builder(), clock);

		assertTrue(tokens.tokenFor(BACKEND).blockOptional().isEmpty());
	}

}
