package co.com.bookhive.initializer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import co.com.bookhive.circulation.infrastructure.input.controllers.SessionController;
import co.com.bookhive.circulation.infrastructure.input.security.SessionCookieTokenResolver;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final String PUBLIC_HEALTH = "/actuator/health/**";
	private static final String PUBLIC_HEALTH_ROOT = "/actuator/health";

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
						.requestMatchers(HttpMethod.POST, SessionController.PATH).permitAll()
						.requestMatchers(HttpMethod.DELETE, SessionController.PATH).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.bearerTokenResolver(tokenResolver)
						.jwt(Customizer.withDefaults()))
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.ignoringRequestMatchers(SessionController.PATH));

		return http.build();
	}

}
