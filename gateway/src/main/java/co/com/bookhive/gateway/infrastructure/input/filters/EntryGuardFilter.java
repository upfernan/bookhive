package co.com.bookhive.gateway.infrastructure.input.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;
import co.com.bookhive.gateway.infrastructure.input.rules.EntryRules;
import co.com.bookhive.gateway.infrastructure.input.security.ClientOrigin;
import co.com.bookhive.gateway.infrastructure.input.security.SessionCookieVerifier;
import co.com.bookhive.gateway.services.input.dto.AttemptDTO;
import co.com.bookhive.gateway.services.input.dto.VerdictDTO;
import co.com.bookhive.gateway.services.input.ports.GuardAttemptsInputPort;
import reactor.core.publisher.Mono;

@Component
public class EntryGuardFilter implements GlobalFilter, Ordered {

	static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 1000;

	private final GuardAttemptsInputPort guard;
	private final EntryRules rules;
	private final SessionCookieVerifier sessions;
	private final ClientOrigin origins;
	private final GatewayResponses responses;

	public EntryGuardFilter(
			final GuardAttemptsInputPort guard,
			final EntryRules rules,
			final SessionCookieVerifier sessions,
			final ClientOrigin origins,
			final GatewayResponses responses) {

		super();
		this.guard = guard;
		this.rules = rules;
		this.sessions = sessions;
		this.origins = origins;
		this.responses = responses;
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {

		final ServerHttpRequest request = exchange.getRequest();
		if (rules.isInternal(request)) {
			return responses.notFound(exchange);
		}

		final boolean publicRoute = rules.isPublic(request);
		final Mono<String> account = publicRoute
				? Mono.just(TextHelper.getDefaultValue())
				: sessions.accountOf(request);

		return account.flatMap(accountId -> {
			final AttemptDTO attempt = new AttemptDTO(
					rules.actionOf(request), accountId, origins.of(exchange), rules.isAdministrative(request));

			return Mono.fromCompletionStage(() -> guard.admit(attempt))
					.onErrorReturn(new VerdictDTO(true, false, 0L))
					.flatMap(verdict -> decide(exchange, chain, attempt, verdict, publicRoute));
		});
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	private Mono<Void> decide(
			final ServerWebExchange exchange,
			final GatewayFilterChain chain,
			final AttemptDTO attempt,
			final VerdictDTO verdict,
			final boolean publicRoute) {

		if (verdict.blocked()) {
			return responses.blocked(exchange, verdict.retryAfterSeconds());
		}

		if (verdict.unavailable()) {
			return responses.unavailable(exchange);
		}

		if (!publicRoute && TextHelper.isDefaultValue(attempt.account())) {
			return responses.withoutSession(exchange)
					.then(Mono.defer(() -> saveOutcome(attempt, HttpStatus.UNAUTHORIZED.value())));
		}

		return chain.filter(exchange)
				.then(Mono.defer(() -> saveOutcome(attempt, statusOf(exchange))));
	}

	private Mono<Void> saveOutcome(final AttemptDTO attempt, final int status) {
		return Mono.fromCompletionStage(() -> guard.recordOutcome(attempt, status))
				.onErrorResume(failure -> Mono.empty());
	}

	private static int statusOf(final ServerWebExchange exchange) {
		final HttpStatusCode status = exchange.getResponse().getStatusCode();
		return status == null ? HttpStatus.OK.value() : status.value();
	}

}
