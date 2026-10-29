package co.com.bookhive.gateway.infrastructure.input.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class ClientOriginTest {

	@Test
	@DisplayName("se toma la dirección que agregó Google, no la que escribió el cliente")
	void lastForwardedAddressIsTrusted() {
		final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
				.header("X-Forwarded-For", "1.2.3.4, 191.91.85.226"));

		assertEquals("191.91.85.226", new ClientOrigin(1).of(exchange));
	}

	@Test
	@DisplayName("con un balanceador delante se confía en un salto más")
	void moreHopsWithLoadBalancer() {
		final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
				.header("X-Forwarded-For", "1.2.3.4, 191.91.85.226, 34.120.0.1"));

		assertEquals("191.91.85.226", new ClientOrigin(2).of(exchange));
	}

	@Test
	@DisplayName("sin encabezado se usa la dirección de la conexión, y si no hay, queda desconocido")
	void fallsBackToRemoteAddress() {
		final MockServerWebExchange direct = MockServerWebExchange.from(MockServerHttpRequest.get("/")
				.remoteAddress(new InetSocketAddress("200.9.159.10", 443)));
		final MockServerWebExchange unknown = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

		assertEquals("200.9.159.10", new ClientOrigin(0).of(direct));
		assertEquals("desconocido", new ClientOrigin(1).of(unknown));
	}

}
