package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;


class SessionCookieDecoderTest {

	private static final String PROJECT = "bookhive-prueba";
	private static final String IDENTITY = "uid-bibliotecario";
	private static final String LOOKUP_URL =
			"https://identitytoolkit.googleapis.com/v1/projects/" + PROJECT + "/accounts:lookup";

	private static RSAKey signingKey;

	private MockRestServiceServer google;
	private SessionCookieDecoder decoder;

	@BeforeAll
	static void createKey() throws JOSEException {
		signingKey = new RSAKeyGenerator(2048).keyID("llave-de-prueba").generate();
	}

	@BeforeEach
	void prepare() {
		final RestClient.Builder builder = RestClient.builder();
		google = MockRestServiceServer.bindTo(builder).build();
		decoder = new SessionCookieDecoder(
				PROJECT,
				new ImmutableJWKSet<>(new JWKSet(signingKey.toPublicJWK())),
				IdentityPlatformClientFactory.create(builder, PROJECT));
	}

	@Test
	@DisplayName("acepta la sesión firmada por Google para el proyecto y todavía abierta")
	void acceptsOpenSession() throws JOSEException {
		expectAccount(1_000L, false);

		final Jwt session = decoder.decode(session(PROJECT, PROJECT, Instant.now().minusSeconds(60)));

		assertEquals(IDENTITY, session.getSubject());
		google.verify();
	}

	@Test
	@DisplayName("rechaza la sesión que se abrió antes de cerrar sesión")
	void rejectsClosedSession() throws JOSEException {
		expectAccount(Instant.now().getEpochSecond(), false);
		final String token = session(PROJECT, PROJECT, Instant.now().minusSeconds(300));

		assertThrows(BadJwtException.class, () -> decoder.decode(token));
	}

	@Test
	@DisplayName("rechaza la sesión de una cuenta deshabilitada")
	void rejectsDisabledAccount() throws JOSEException {
		expectAccount(0L, true);
		final String token = session(PROJECT, PROJECT, Instant.now().minusSeconds(60));

		assertThrows(BadJwtException.class, () -> decoder.decode(token));
	}

	@Test
	@DisplayName("sin hora de inicio usa la hora en que se emitió la sesión")
	void usesIssuedAtWhenAuthTimeIsMissing() throws JOSEException {
		expectAccount(1_000L, false);

		final Jwt session = decoder.decode(session(PROJECT, PROJECT, null));

		assertEquals(IDENTITY, session.getSubject());
	}

	@Test
	@DisplayName("rechaza la sesión emitida para otro proyecto")
	void rejectsOtherAudience() throws JOSEException {
		final String token = session(PROJECT, "otro-proyecto", Instant.now());

		assertThrows(JwtException.class, () -> decoder.decode(token));
	}

	@Test
	@DisplayName("rechaza la sesión de otro emisor")
	void rejectsOtherIssuer() throws JOSEException {
		final String token = session("otro-proyecto", PROJECT, Instant.now());

		assertThrows(JwtException.class, () -> decoder.decode(token));
	}

	@Test
	@DisplayName("si no puede confirmar con Google que la sesión sigue abierta la rechaza")
	void rejectsWhenGoogleCannotConfirm() throws JOSEException {
		google.expect(requestTo(IdentityPlatformClientFactory.TOKEN_URL))
				.andRespond(withSuccess("{\"access_token\":\"acceso\",\"expires_in\":3599}", MediaType.APPLICATION_JSON));
		google.expect(requestTo(LOOKUP_URL)).andRespond(withServerError());
		final String token = session(PROJECT, PROJECT, Instant.now());

		assertThrows(JwtException.class, () -> decoder.decode(token));
	}

	@Test
	@DisplayName("sin proyecto configurado ninguna sesión es válida")
	void rejectsEverythingWithoutProject() throws JOSEException {
		final SessionCookieDecoder unconfigured = new SessionCookieDecoder(
				" ",
				new ImmutableJWKSet<>(new JWKSet(signingKey.toPublicJWK())),
				IdentityPlatformClientFactory.create(RestClient.builder(), " "));
		final String token = session(PROJECT, PROJECT, Instant.now());

		assertThrows(BadJwtException.class, () -> unconfigured.decode(token));
	}

	private void expectAccount(final long validSince, final boolean disabled) {
		google.expect(requestTo(IdentityPlatformClientFactory.TOKEN_URL))
				.andRespond(withSuccess("{\"access_token\":\"acceso\",\"expires_in\":3599}", MediaType.APPLICATION_JSON));
		google.expect(requestTo(LOOKUP_URL))
				.andRespond(withSuccess(
						"{\"users\":[{\"localId\":\"" + IDENTITY + "\",\"validSince\":\"" + validSince
								+ "\",\"disabled\":" + disabled + "}]}",
						MediaType.APPLICATION_JSON));
	}

	private static String session(final String issuerProject, final String audience, final Instant authTime)
			throws JOSEException {

		final Instant now = Instant.now();
		final JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
				.issuer(SessionCookieDecoder.ISSUER_PREFIX + issuerProject)
				.audience(audience)
				.subject(IDENTITY)
				.issueTime(Date.from(now.minusSeconds(30)))
				.expirationTime(Date.from(now.plusSeconds(1200)));

		if (authTime != null) {
			claims.claim("auth_time", authTime.getEpochSecond());
		}

		final SignedJWT jwt = new SignedJWT(
				new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(),
				claims.build());
		jwt.sign(new RSASSASigner(signingKey));

		return jwt.serialize();
	}

}
