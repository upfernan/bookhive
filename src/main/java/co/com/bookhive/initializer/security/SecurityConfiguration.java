package co.com.bookhive.initializer.security;

import org.springframework.beans.factory.annotation.Value;
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
import co.com.bookhive.payment.infrastructure.input.controllers.WompiEventsController;
import co.com.bookhive.access.infrastructure.input.security.SessionCookieTokenResolver;
import co.com.bookhive.access.infrastructure.output.externalservices.identity.ServiceCallerDecoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final String PUBLIC_HEALTH = "/actuator/health/**";
	private static final String PUBLIC_HEALTH_ROOT = "/actuator/health";

	@Bean
	@Order(1)
	@SuppressWarnings("java:S4502")
	SecurityFilterChain internalCallsFilterChain(
			final HttpSecurity http,
			final ServiceCallerDecoder serviceCallerDecoder,
			@Value("${bookhive.tasks.trust-cloud-run-iam:false}") final boolean trustCloudRunIam) {

		http.securityMatcher(InternalPaths.all())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.csrf(csrf -> csrf.ignoringRequestMatchers(InternalPaths.all()));

		if (trustCloudRunIam) {
			http.authorizeHttpRequests(requests -> requests.anyRequest().access(new InternalCallerAuthorization()));
		} else {
			http.authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
					.oauth2ResourceServer(resourceServer -> resourceServer
							.jwt(jwt -> jwt.decoder(serviceCallerDecoder::decode)));
		}
		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain securityFilterChain(
			final HttpSecurity http,
			final SessionCookieTokenResolver tokenResolver) {

		http.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers -> headers
						.httpStrictTransportSecurity(Customizer.withDefaults())
						.frameOptions(frames -> frames.deny()));

		http.authorizeHttpRequests(requests -> requests
						.requestMatchers(HttpMethod.GET, PUBLIC_HEALTH, PUBLIC_HEALTH_ROOT).permitAll()
						.requestMatchers(HttpMethod.POST, SessionController.PATH).permitAll()
						.requestMatchers(HttpMethod.DELETE, SessionController.PATH).permitAll()
						.requestMatchers(HttpMethod.POST, WompiEventsController.PATH).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.bearerTokenResolver(tokenResolver)
						.jwt(Customizer.withDefaults()))
				.csrf(csrf -> csrf.spa().ignoringRequestMatchers(WompiEventsController.PATH));

		return http.build();
	}

}
