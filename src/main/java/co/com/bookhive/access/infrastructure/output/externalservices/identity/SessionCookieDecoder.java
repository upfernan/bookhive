package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

@Component
public class SessionCookieDecoder implements JwtDecoder {

	static final String ISSUER_PREFIX = "https://session.firebase.google.com/";

	private static final String AUTH_TIME = "auth_time";

	private final String projectId;
	private final IdentityPlatformClient identityPlatform;
	private final NimbusJwtDecoder signatureDecoder;

	public SessionCookieDecoder(
			@Value("${bookhive.identity.project-id:}") final String projectId,
			final JWKSource<SecurityContext> sessionKeys,
			final IdentityPlatformClient identityPlatform) {

		super();
		this.projectId = TextHelper.getDefaultValue(projectId);
		this.identityPlatform = identityPlatform;
		this.signatureDecoder = NimbusJwtDecoder.withJwkSource(sessionKeys)
				.jwsAlgorithm(SignatureAlgorithm.RS256)
				.build();
		this.signatureDecoder.setJwtValidator(JwtValidators.createDefaultWithValidators(List.of(
				JwtValidators.createDefaultWithIssuer(ISSUER_PREFIX + this.projectId),
				audienceIsProject())));
	}

	@Override
	public Jwt decode(final String token) {

		if (TextHelper.isDefaultValue(projectId)) {
			throw new BadJwtException("No hay proveedor de identidad configurado para validar la sesión (ADR-026).");
		}

		final Jwt session = signatureDecoder.decode(token);

		if (!accountOf(session).acceptsSessionStartedAt(authTimeOf(session))) {
			throw new BadJwtException("La sesión fue cerrada o la cuenta está deshabilitada.");
		}

		return session;
	}

	private IdentityPlatformAccount accountOf(final Jwt session) {

		try {
			return identityPlatform.lookup(session.getSubject());
		} catch (final BookHiveException exception) {
			throw new JwtException("No se pudo confirmar con Identity Platform que la sesión siga abierta.", exception);
		}
	}

	private static long authTimeOf(final Jwt session) {

		final Object authTime = session.getClaims().get(AUTH_TIME);

		if (authTime instanceof Number number) {
			return number.longValue();
		}

		if (authTime instanceof Instant instant) {
			return instant.getEpochSecond();
		}

		final Instant issuedAt = session.getIssuedAt();

		return ObjectHelper.isNull(issuedAt) ? 0L : issuedAt.getEpochSecond();
	}

	private OAuth2TokenValidator<Jwt> audienceIsProject() {

		return session -> {
			final List<String> audience = session.getAudience();

			if (ObjectHelper.isNull(audience) || !audience.contains(projectId)) {
				return OAuth2TokenValidatorResult.failure(
						new OAuth2Error("invalid_token", "La sesión no fue emitida para este proyecto.", null));
			}

			return OAuth2TokenValidatorResult.success();
		};
	}

}
