package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class ServiceCallerDecoderTest {

	private static final String AUDIENCE = "https://bookhive-api-164070755294.us-east1.run.app";
	private static final String CALLER = "bookhive-tareas@bookhive-511618.iam.gserviceaccount.com";

	@Test
	@DisplayName("acepta el pase de Google emitido para este servicio a una cuenta autorizada")
	void acceptsAuthorizedCaller() {
		final ServiceCallerDecoder decoder = new ServiceCallerDecoder(AUDIENCE, " OTRA@x.com , " + CALLER.toUpperCase(),
				decoding(jwt -> { }));

		assertEquals(CALLER, decoder.decode("pase").getClaimAsString("email"));
	}

	@Test
	@DisplayName("rechaza pases de otro emisor, para otro servicio, de otra cuenta o sin correo verificado")
	void rejectsUntrustedCallers() {
		final List<JwtDecoder> untrusted = List.of(
				decoding(jwt -> jwt.issuer("https://evil.example.com")),
				decoding(jwt -> jwt.audience(List.of("https://otro-servicio.run.app"))),
				decoding(jwt -> jwt.claim("email", "intruso@bookhive-511618.iam.gserviceaccount.com")),
				decoding(jwt -> jwt.claim("email_verified", false)));

		for (final JwtDecoder signature : untrusted) {
			final ServiceCallerDecoder decoder = new ServiceCallerDecoder(AUDIENCE, CALLER, signature);
			assertThrows(BadJwtException.class, () -> decoder.decode("pase"));
		}
	}

	@Test
	@DisplayName("sin audiencia o sin cuentas autorizadas no deja pasar a nadie")
	void rejectsEverythingWhenNotConfigured() {
		final ServiceCallerDecoder withoutAudience = new ServiceCallerDecoder("", CALLER, decoding(jwt -> { }));
		final ServiceCallerDecoder withoutCallers = new ServiceCallerDecoder(AUDIENCE, " , ", decoding(jwt -> { }));

		assertThrows(BadJwtException.class, () -> withoutAudience.decode("pase"));
		assertThrows(BadJwtException.class, () -> withoutCallers.decode("pase"));
	}

	@Test
	@DisplayName("acepta el emisor de Google con o sin https")
	void acceptsBothGoogleIssuers() {
		final ServiceCallerDecoder decoder = new ServiceCallerDecoder(AUDIENCE, CALLER,
				decoding(jwt -> jwt.issuer("accounts.google.com")));

		assertEquals(CALLER, decoder.decode("pase").getClaimAsString("email"));
	}

	private static JwtDecoder decoding(final Consumer<Jwt.Builder> change) {
		return token -> {
			final Jwt.Builder jwt = Jwt.withTokenValue(token)
					.header("alg", "RS256")
					.issuer("https://accounts.google.com")
					.audience(List.of(AUDIENCE))
					.claim("email", CALLER)
					.claim("email_verified", true)
					.issuedAt(Instant.now())
					.expiresAt(Instant.now().plusSeconds(300));
			change.accept(jwt);
			return jwt.build();
		};
	}

}
