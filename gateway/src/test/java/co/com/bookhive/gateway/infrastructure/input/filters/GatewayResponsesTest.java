package co.com.bookhive.gateway.infrastructure.input.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class GatewayResponsesTest {

	private final GatewayResponses responses = new GatewayResponses(new GatewayMessagesProperties(null, "Espera \"un\" rato", ""));

	@Test
	@DisplayName("el bloqueo responde 429 con la espera y el mensaje en JSON")
	void blockedAnswersTooManyRequests() {
		final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/prestamos"));

		responses.blocked(exchange, 120).block();

		assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
		assertEquals("120", exchange.getResponse().getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
		assertEquals("{\"message\":\"Espera \\\"un\\\" rato\"}", exchange.getResponse().getBodyAsString().block());
	}

	@Test
	@DisplayName("sin sesión responde 401 y sin Redis en lo administrativo responde 503, con los textos por defecto")
	void defaultsForOtherAnswers() {
		final MockServerWebExchange withoutSession = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/x"));
		final MockServerWebExchange unavailable = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/x"));

		responses.withoutSession(withoutSession).block();
		responses.unavailable(unavailable).block();

		assertEquals(HttpStatus.UNAUTHORIZED, withoutSession.getResponse().getStatusCode());
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, unavailable.getResponse().getStatusCode());
		assertEquals("{\"message\":\"" + GatewayMessagesProperties.DEFAULT_WITHOUT_SESSION + "\"}",
				withoutSession.getResponse().getBodyAsString().block());
	}

	@Test
	@DisplayName("el texto se escapa para que el JSON nunca se rompa")
	void escapesControlCharacters() {
		assertEquals("a\\\\b\\n\\r\\t c", GatewayResponses.escape("a\\b\n\r\t\u0001c"));
	}

}
