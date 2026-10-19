package co.com.bookhive.initializer.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import co.com.bookhive.circulation.infrastructure.input.security.SessionCookieTokenResolver;
import co.com.bookhive.crosscutting.helper.TextHelper;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

	private static final String PUBLIC_HEALTH = "/actuator/health/**";
	private static final String PUBLIC_HEALTH_ROOT = "/actuator/health";

	private final String jwkSetUri;

	public SecurityConfiguration(
			@Value("${bookhive.security.jwk-set-uri:}") final String jwkSetUri) {

		super();
		this.jwkSetUri = TextHelper.getDefaultValue(jwkSetUri);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			final HttpSecurity http,
			final SessionCookieTokenResolver tokenResolver) throws Exception {

		http.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers -> headers
						.httpStrictTransportSecurity(Customizer.withDefaults())
						.frameOptions(frames -> frames.deny()));

		http.authorizeHttpRequests(requests -> requests
						.requestMatchers(HttpMethod.GET, PUBLIC_HEALTH, PUBLIC_HEALTH_ROOT).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.bearerTokenResolver(tokenResolver)
						.jwt(Customizer.withDefaults()))
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder() {

		if (TextHelper.isDefaultValue(jwkSetUri)) {
			logger.warn("Sin proveedor de identidad configurado: falta bookhive.security.jwk-set-uri,"
					+ " asi que toda solicitud autenticada sera rechazada (ADR-026).");

			return token -> {
				throw new BadJwtException(
						"No hay proveedor de identidad configurado para validar el pase.");
			};
		}

		return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
	}

}
