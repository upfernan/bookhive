package co.com.bookhive.gateway.infrastructure.input.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import co.com.bookhive.gateway.crosscutting.helper.TextHelper;
import co.com.bookhive.gateway.infrastructure.output.identity.MetadataIdentityTokens;
import reactor.core.publisher.Mono;

@Component
public class BackendIdentityFilter implements GlobalFilter, Ordered {

	static final String SERVERLESS_AUTHORIZATION = "X-Serverless-Authorization";
	static final int ORDER = 0;

	private static final String BEARER = "Bearer ";
	private static final Logger logger = LoggerFactory.getLogger(BackendIdentityFilter.class);

	private final MetadataIdentityTokens tokens;
	private final GatewayResponses responses;
	private final boolean enabled;
	private final String audience;

	public BackendIdentityFilter(
			final MetadataIdentityTokens tokens,
			final GatewayResponses responses,
			@Value("${bookhive.gateway.backend-authentication:false}") final boolean enabled,
			@Value("${bookhive.gateway.backend-url:}") final String audience) {

		super();
		this.tokens = tokens;
		this.responses = responses;
		this.enabled = enabled;
		this.audience = TextHelper.getDefaultValue(audience);
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {

		if (!enabled) {
			return chain.filter(withHeader(exchange, TextHelper.getDefaultValue()));
		}

		return tokens.tokenFor(audience)
				.onErrorResume(failure -> {
					logger.warn("No se pudo obtener la identidad de la puerta para llamar al backend: {}", failure.getMessage());
					return Mono.empty();
				})
				.defaultIfEmpty(TextHelper.getDefaultValue())
				.flatMap(token -> TextHelper.isDefaultValue(token)
						? responses.unavailable(exchange)
						: chain.filter(withHeader(exchange, token)));
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	static ServerWebExchange withHeader(final ServerWebExchange exchange, final String token) {
		return exchange.mutate()
				.request(request -> request.headers(headers -> {
					headers.remove(SERVERLESS_AUTHORIZATION);

					if (!TextHelper.isDefaultValue(token)) {
						headers.set(SERVERLESS_AUTHORIZATION, BEARER + token);
					}
				}))
				.build();
	}

}
