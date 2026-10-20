package co.com.bookhive.gateway.infrastructure.input.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import co.com.bookhive.gateway.infrastructure.output.identity.MetadataIdentityTokens;
import reactor.core.publisher.Mono;

class BackendIdentityFilterTest {

	private static final String BACKEND = "https://bookhive-api-164070755294.us-east1.run.app";

	private final AtomicReference<ServerWebExchange> received = new AtomicReference<>();
	private final GatewayFilterChain chain = exchange -> {
		received.set(exchange);
		return Mono.empty();
	};
	private final GatewayResponses responses = new GatewayResponses(new GatewayMessagesProperties(null, null, null));

	private static MockServerWebExchange requestWithForgedHeader() {
		return MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/prestamos")
				.header(BackendIdentityFilter.SERVERLESS_AUTHORIZATION, "Bearer falso"));
	}

	private static MetadataIdentityTokens tokensAnswering(final Mono<String> token) {
		return new MetadataIdentityTokens(WebClient.builder(), Clock.systemUTC()) {

			@Override
			public Mono<String> tokenFor(final String audience) {
				return token;
			}

		};
	}

	@Test
	@DisplayName("apagado, solo quita el encabezado que pudo mandar el cliente")
	void disabledRemovesClientHeader() {
		new BackendIdentityFilter(tokensAnswering(Mono.just("no-se-usa")), responses, false, BACKEND)
				.filter(requestWithForgedHeader(), chain).block();

		assertNull(received.get().getRequest().getHeaders().getFirst(BackendIdentityFilter.SERVERLESS_AUTHORIZATION));
	}

	@Test
	@DisplayName("encendido, reemplaza el encabezado con la identidad de la puerta")
	void enabledSetsGatewayIdentity() {
		new BackendIdentityFilter(tokensAnswering(Mono.just("pase-de-la-puerta")), responses, true, BACKEND)
				.filter(requestWithForgedHeader(), chain).block();

		assertEquals("Bearer pase-de-la-puerta",
				received.get().getRequest().getHeaders().getFirst(BackendIdentityFilter.SERVERLESS_AUTHORIZATION));
	}

	@Test
	@DisplayName("si no consigue la identidad responde 503 y no llama al backend")
	void missingIdentityAnswersUnavailable() {
		final MockServerWebExchange failing = requestWithForgedHeader();
		final MockServerWebExchange empty = requestWithForgedHeader();

		new BackendIdentityFilter(tokensAnswering(Mono.error(new IllegalStateException("sin metadatos"))), responses, true, BACKEND)
				.filter(failing, chain).block();
		new BackendIdentityFilter(tokensAnswering(Mono.empty()), responses, true, BACKEND)
				.filter(empty, chain).block();

		assertNull(received.get());
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, failing.getResponse().getStatusCode());
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, empty.getResponse().getStatusCode());
	}

	@Test
	@DisplayName("corre después del guardián de entrada")
	void runsAfterEntryGuard() {
		final BackendIdentityFilter filter = new BackendIdentityFilter(tokensAnswering(Mono.empty()), responses, false, BACKEND);

		assertTrue(filter.getOrder() > EntryGuardFilter.ORDER);
		assertNotEquals(Integer.MAX_VALUE, filter.getOrder());
	}

}
