package co.com.bookhive.gateway.initializer.configuration;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import co.com.bookhive.gateway.domain.attempt.AttemptPolicy;
import co.com.bookhive.gateway.infrastructure.input.filters.GatewayMessagesProperties;
import co.com.bookhive.gateway.infrastructure.input.rules.EntryRulesProperties;
import co.com.bookhive.gateway.services.input.adapters.GuardAttemptsInteractor;
import co.com.bookhive.gateway.services.input.ports.GuardAttemptsInputPort;
import co.com.bookhive.gateway.services.output.ports.AttemptStore;
import co.com.bookhive.gateway.services.output.ports.AuditPort;
import co.com.bookhive.gateway.services.usecase.guardattempts.GuardAttemptsUseCase;
import co.com.bookhive.gateway.services.usecase.guardattempts.impl.GuardAttemptsUseCaseImpl;

@Configuration
@EnableConfigurationProperties({
		AttemptLimitsProperties.class,
		EntryRulesProperties.class,
		GatewayMessagesProperties.class })
public class GuardConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	AttemptPolicy attemptPolicy(final AttemptLimitsProperties limits) {
		return new AttemptPolicy(limits.maxPerAccount(), limits.maxPerOrigin(), limits.window(), limits.blockFor());
	}

	@Bean
	GuardAttemptsUseCase guardAttemptsUseCase(
			final AttemptPolicy policy,
			final AttemptStore store,
			final AuditPort audit) {

		return new GuardAttemptsUseCaseImpl(policy, store, audit);
	}

	@Bean
	GuardAttemptsInputPort guardAttemptsInputPort(final GuardAttemptsUseCase useCase) {
		return new GuardAttemptsInteractor(useCase);
	}

}
