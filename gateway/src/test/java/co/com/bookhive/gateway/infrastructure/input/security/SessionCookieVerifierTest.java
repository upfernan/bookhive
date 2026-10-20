package co.com.bookhive.gateway.infrastructure.input.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import co.com.bookhive.gateway.infrastructure.output.identity.SessionKeys;
import co.com.bookhive.gateway.testdata.SessionTokens;
import reactor.core.publisher.Mono;

class SessionCookieVerifierTest {

	private static final Clock CLOCK = Clock.fixed(SessionTokens.NOW, ZoneOffset.UTC);
	private static final SessionKeys KNOWN_KEY = keyId -> SessionTokens.KEY_ID.equals(keyId)
			? Mono.just(SessionTokens.publicKey())
			: Mono.empty();

	private final SessionCookieVerifier verifier = new SessionCookieVerifier(SessionTokens.PROJECT, KNOWN_KEY, CLOCK);

	private String accountOf(final String token) {
		return verifier.accountOf(token).block();
	}

	@Test
	@DisplayName("una cookie de sesión válida entrega el número de cuenta")
	void validSessionGivesAccount() {
		final MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/prestamos")
				.cookie(new HttpCookie(SessionCookieVerifier.SESSION_COOKIE, SessionTokens.valid()))
				.build();

		assertEquals(SessionTokens.ACCOUNT, verifier.accountOf(request).block());
	}

	@Test
	@DisplayName("sin cookie, o con una que no es un pase, no hay cuenta")
	void missingOrGarbageCookieGivesNoAccount() {
		assertEquals("", verifier.accountOf(MockServerHttpRequest.get("/").build()).block());
		assertEquals("", accountOf("esto-no-es-un-pase"));
		assertEquals("", accountOf("   "));
	}

	@Test
	@DisplayName("un pase firmado con otra llave, o con un secreto, no sirve")
	void foreignSignaturesAreRejected() {
		assertEquals("", accountOf(SessionTokens.signedByOtherKey(SessionTokens.validClaims().build())));
		assertEquals("", accountOf(SessionTokens.signedWithSecret(SessionTokens.validClaims().build())));
	}

	@Test
	@DisplayName("un pase de otro emisor, de otro proyecto o sin cuenta no sirve")
	void wrongClaimsAreRejected() {
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims()
				.issuer("https://securetoken.google.com/" + SessionTokens.PROJECT).build())));
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims().audience("otro-proyecto").build())));
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims().subject(" ").build())));
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims().subject("x".repeat(129)).build())));
	}

	@Test
	@DisplayName("un pase vencido o emitido en el futuro no sirve")
	void timeIsChecked() {
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims()
				.expirationTime(Date.from(SessionTokens.NOW.minusSeconds(120))).build())));
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims()
				.expirationTime(null).build())));
		assertEquals("", accountOf(SessionTokens.signed(SessionTokens.validClaims()
				.issueTime(Date.from(SessionTokens.NOW.plusSeconds(600))).build())));
		assertEquals(SessionTokens.ACCOUNT, accountOf(SessionTokens.signed(SessionTokens.validClaims()
				.issueTime(null).build())));
	}

	@Test
	@DisplayName("si no hay proyecto configurado, o no se encuentran las llaves, no hay cuenta")
	void missingConfigurationOrKeysGiveNoAccount() {
		final SessionCookieVerifier withoutProject = new SessionCookieVerifier("", KNOWN_KEY, CLOCK);
		final SessionCookieVerifier keysDown = new SessionCookieVerifier(SessionTokens.PROJECT,
				keyId -> Mono.error(new IllegalStateException("sin red")), CLOCK);
		final SessionCookieVerifier unknownKey = new SessionCookieVerifier(SessionTokens.PROJECT,
				keyId -> Mono.empty(), CLOCK);

		assertEquals("", withoutProject.accountOf(SessionTokens.valid()).block());
		assertEquals("", keysDown.accountOf(SessionTokens.valid()).block());
		assertEquals("", unknownKey.accountOf(SessionTokens.valid()).block());
	}

}
