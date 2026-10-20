package co.com.bookhive.gateway.infrastructure.input.filters;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GatewayResponses {

	private final GatewayMessagesProperties messages;

	public GatewayResponses(final GatewayMessagesProperties messages) {
		super();
		this.messages = messages;
	}

	public Mono<Void> withoutSession(final ServerWebExchange exchange) {
		return write(exchange, HttpStatus.UNAUTHORIZED, messages.withoutSession());
	}

	public Mono<Void> blocked(final ServerWebExchange exchange, final long retryAfterSeconds) {
		exchange.getResponse().getHeaders().set(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1L, retryAfterSeconds)));
		return write(exchange, HttpStatus.TOO_MANY_REQUESTS, messages.blocked());
	}

	public Mono<Void> unavailable(final ServerWebExchange exchange) {
		return write(exchange, HttpStatus.SERVICE_UNAVAILABLE, messages.unavailable());
	}

	private static Mono<Void> write(final ServerWebExchange exchange, final HttpStatus status, final String message) {

		final ServerHttpResponse response = exchange.getResponse();

		if (response.isCommitted()) {
			return Mono.empty();
		}

		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		final DataBuffer body = response.bufferFactory()
				.wrap(("{\"message\":\"" + escape(message) + "\"}").getBytes(StandardCharsets.UTF_8));

		return response.writeWith(Mono.just(body));
	}

	static String escape(final String text) {

		final StringBuilder escaped = new StringBuilder(text.length());

		for (final char character : text.toCharArray()) {
			switch (character) {
				case '"' -> escaped.append("\\\"");
				case '\\' -> escaped.append("\\\\");
				case '\n' -> escaped.append("\\n");
				case '\r' -> escaped.append("\\r");
				case '\t' -> escaped.append("\\t");
				default -> escaped.append(character < ' ' ? ' ' : character);
			}
		}

		return escaped.toString();
	}

}
