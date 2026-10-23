package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

@Component
public class ServiceCallerDecoder {

	static final String GOOGLE_CERTS = "https://www.googleapis.com/oauth2/v3/certs";
	static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

	private final String audience;
	private final Set<String> callers;
	private final JwtDecoder signatureDecoder;

	@Autowired
	public ServiceCallerDecoder(
			@Value("${bookhive.tasks.audience:}") final String audience,
			@Value("${bookhive.tasks.callers:}") final String callers) {

		this(audience, callers, NimbusJwtDecoder.withJwkSetUri(GOOGLE_CERTS).build());
	}

	ServiceCallerDecoder(final String audience, final String callers, final JwtDecoder signatureDecoder) {
		super();
		this.audience = TextHelper.getDefaultValue(audience);
		this.callers = Arrays.stream(TextHelper.getDefaultValue(callers).split(","))
				.map(caller -> caller.trim().toLowerCase(Locale.ROOT))
				.filter(caller -> !caller.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
		this.signatureDecoder = signatureDecoder;
	}

	public Jwt decode(final String token) {
		if (TextHelper.isDefaultValue(audience) || callers.isEmpty()) {
			throw new BadJwtException("No hay audiencia ni cuentas autorizadas para las tareas internas (ADR-035).");
		}
		final Jwt jwt = signatureDecoder.decode(token);
		final OAuth2TokenValidatorResult result = fromGoogleForUs().validate(jwt);
		if (result.hasErrors()) {
			throw new BadJwtException(TextHelper.getDefaultValue(result.getErrors().iterator().next().getDescription()));
		}
		return jwt;
	}

	private OAuth2TokenValidator<Jwt> fromGoogleForUs() {
		return jwt -> {
			final String issuer = TextHelper.getDefaultValue(jwt.getClaimAsString("iss"));
			if (!GOOGLE_ISSUERS.contains(issuer)) {
				return failure("El pase no lo emitió Google.");
			}
			final List<String> audiences = ObjectHelper.getDefaultValue(jwt.getAudience(), List.<String>of());
			if (!audiences.contains(audience)) {
				return failure("El pase no fue emitido para este servicio.");
			}
			final String email = TextHelper.getDefaultValue(jwt.getClaimAsString("email")).toLowerCase(Locale.ROOT);
			if (!callers.contains(email)) {
				return failure("La cuenta que llama no está autorizada para las tareas internas.");
			}
			if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
				return failure("La cuenta que llama no tiene el correo verificado.");
			}
			return OAuth2TokenValidatorResult.success();
		};
	}

	private static OAuth2TokenValidatorResult failure(final String description) {
		return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
	}

}
