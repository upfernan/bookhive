package co.com.bookhive.gateway.infrastructure.output.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import co.com.bookhive.gateway.testdata.MutableClock;
import co.com.bookhive.gateway.testdata.SessionTokens;
import co.com.bookhive.gateway.testdata.StubHttp;
import reactor.core.publisher.Mono;

class GoogleSessionKeysTest {

	private static final String RSA_CERTIFICATE = resource("/certificados/sesion-rsa.pem");
	private static final String EC_CERTIFICATE = resource("/certificados/sesion-ec.pem");

	private final MutableClock clock = new MutableClock(SessionTokens.NOW);

	private static String resource(final String name) {
		try (InputStream input = GoogleSessionKeysTest.class.getResourceAsStream(name)) {
			return new String(input.readAllBytes(), StandardCharsets.US_ASCII);
		} catch (final IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private static String json(final String keyId, final String pem) {
		return "{\"" + keyId + "\":\"" + pem.replace("\r", "").replace("\n", "\\n") + "\"}";
	}

	@Test
	@DisplayName("descarga los certificados de Google y los guarda una hora")
	void downloadsAndCachesForAnHour() {
		final StubHttp http = StubHttp.answering(HttpStatus.OK, MediaType.APPLICATION_JSON, json("llave-1", RSA_CERTIFICATE));
		final GoogleSessionKeys keys = new GoogleSessionKeys(http.builder(), clock);

		assertNotNull(keys.find("llave-1").block());
		clock.advance(Duration.ofMinutes(59));
		assertNotNull(keys.find("llave-1").block());
		assertEquals(1, http.requests().size());
		assertEquals(GoogleSessionKeys.KEYS_URL, http.requests().get(0).url().toString());

		clock.advance(Duration.ofMinutes(2));
		assertNotNull(keys.find("llave-1").block());
		assertEquals(2, http.requests().size());
	}

	@Test
	@DisplayName("una llave desconocida no dispara descargas seguidas: espera al menos un minuto")
	void unknownKeyDoesNotHammerGoogle() {
		final StubHttp http = StubHttp.answering(HttpStatus.OK, MediaType.APPLICATION_JSON, json("llave-1", RSA_CERTIFICATE));
		final GoogleSessionKeys keys = new GoogleSessionKeys(http.builder(), clock);

		assertNull(keys.find("inventada").block());
		assertNull(keys.find("inventada").block());
		assertEquals(1, http.requests().size());

		clock.advance(Duration.ofMinutes(2));
		assertNull(keys.find("inventada").block());
		assertEquals(2, http.requests().size());
	}

	@Test
	@DisplayName("si Google no responde, la llave no se encuentra")
	void downloadFailureFindsNothing() {
		final GoogleSessionKeys keys = new GoogleSessionKeys(StubHttp.failing().builder(), clock);

		assertTrue(keys.find("llave-1").onErrorResume(failure -> Mono.empty()).blockOptional().isEmpty());
	}

	@Test
	@DisplayName("solo se aceptan certificados RSA que se puedan leer")
	void onlyReadableRsaCertificatesAreKept() {
		final Map<String, String> certificates = new HashMap<>();
		certificates.put("rsa", RSA_CERTIFICATE);
		certificates.put("ec", EC_CERTIFICATE);
		certificates.put("roto", "-----BEGIN CERTIFICATE-----\nnada\n-----END CERTIFICATE-----");

		assertEquals(Set.of("rsa"), GoogleSessionKeys.toKeys(certificates).keySet());
		assertTrue(GoogleSessionKeys.toKeys(null).isEmpty());
	}

}
