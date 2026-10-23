package co.com.bookhive.initializer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import co.com.bookhive.access.infrastructure.input.controllers.SessionController;
import co.com.bookhive.access.infrastructure.input.security.SessionCookieTokenResolver;
import co.com.bookhive.access.infrastructure.output.externalservices.identity.ServiceCallerDecoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final String PUBLIC_HEALTH = "/actuator/health/**";
	private static final String PUBLIC_HEALTH_ROOT = "/actuator/health";

	@Bean
	@Order(1)
	SecurityFilterChain internalCallsFilterChain(
			final HttpSecurity http,
			final ServiceCallerDecoder serviceCallerDecoder) throws Exception {

		http.securityMatcher(InternalPaths.all())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.jwt(jwt -> jwt.decoder(serviceCallerDecoder::decode)))
				.csrf(csrf -> csrf.ignoringRequestMatchers(InternalPaths.all()));
		return http.build();
	}

	@Bean
	@Order(2)
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
						.requestMatchers(HttpMethod.POST, SessionController.PATH).permitAll()
						.requestMatchers(HttpMethod.DELETE, SessionController.PATH).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.bearerTokenResolver(tokenResolver)
						.jwt(Customizer.withDefaults()))
				.csrf(csrf -> csrf.spa());

		return http.build();
	}

}
