package co.com.bookhive.circulation.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;

class GoogleSessionKeysTest {

	private MockRestServiceServer google;
	private GoogleSessionKeys keys;

	@BeforeEach
	void prepare() {
		final RestClient.Builder builder = RestClient.builder();
		google = MockRestServiceServer.bindTo(builder).build();
		keys = new GoogleSessionKeys(builder);
	}

	@Test
	@DisplayName("convierte los certificados de Google en llaves con su identificador")
	void readsCertificatesAsKeys() throws IOException, KeySourceException {
		google.expect(ExpectedCount.once(), requestTo(GoogleSessionKeys.KEYS_URL))
				.andRespond(withSuccess(certificates("llave-1"), MediaType.APPLICATION_JSON));

		final List<JWK> found = keys.get(selectorFor("llave-1"), null);
		final List<JWK> again = keys.get(selectorFor("llave-1"), null);

		assertEquals(1, found.size());
		assertEquals("llave-1", found.get(0).getKeyID());
		assertEquals(KeyType.RSA, found.get(0).getKeyType());
		assertEquals(found, again);
		google.verify();
	}

	@Test
	@DisplayName("si Google cambió de llave las vuelve a descargar")
	void reloadsWhenKeyIsUnknown() throws IOException, KeySourceException {
		google.expect(ExpectedCount.twice(), requestTo(GoogleSessionKeys.KEYS_URL))
				.andRespond(withSuccess(certificates("llave-1"), MediaType.APPLICATION_JSON));

		assertTrue(keys.get(selectorFor("llave-nueva"), null).isEmpty());
		google.verify();
	}

	@Test
	@DisplayName("si Google no responde avisa que no hay llaves")
	void failsWhenGoogleIsDown() {
		google.expect(requestTo(GoogleSessionKeys.KEYS_URL)).andRespond(withServerError());
		final JWKSelector selector = selectorFor("llave-1");

		assertThrows(KeySourceException.class, () -> keys.get(selector, null));
	}

	@Test
	@DisplayName("un certificado dañado no se acepta")
	void rejectsBrokenCertificate() {
		final Map<String, String> broken = Map.of("llave-1", "no es un certificado");

		assertThrows(KeySourceException.class, () -> GoogleSessionKeys.toKeySet(broken));
	}

	@Test
	@DisplayName("sin respuesta no hay llaves")
	void emptyResponseHasNoKeys() throws KeySourceException {
		assertTrue(GoogleSessionKeys.toKeySet(null).getKeys().isEmpty());
	}

	private static JWKSelector selectorFor(final String keyId) {
		return new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
	}

	private static String certificates(final String keyId) throws IOException {
		try (InputStream pem = Objects.requireNonNull(
				GoogleSessionKeysTest.class.getResourceAsStream("/identity/session-key-certificate.pem"))) {
			final String certificate = new String(pem.readAllBytes(), StandardCharsets.US_ASCII);
			return "{\"" + keyId + "\":\"" + certificate.replace("\n", "\\n") + "\"}";
		}
	}

}
