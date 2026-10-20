package co.com.bookhive.gateway.testdata;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public final class StubHttp {

	private final List<ClientRequest> requests = new ArrayList<>();
	private final Supplier<Mono<ClientResponse>> answer;

	private StubHttp(final Supplier<Mono<ClientResponse>> answer) {
		this.answer = answer;
	}

	public static StubHttp answering(final HttpStatus status, final MediaType type, final String body) {
		return new StubHttp(() -> Mono.just(ClientResponse.create(status)
				.header(HttpHeaders.CONTENT_TYPE, type.toString())
				.body(body)
				.build()));
	}

	public static StubHttp failing() {
		return new StubHttp(() -> Mono.error(new IllegalStateException("sin red")));
	}

	public WebClient.Builder builder() {
		return WebClient.builder().exchangeFunction(request -> {
			requests.add(request);
			return answer.get();
		});
	}

	public List<ClientRequest> requests() {
		return List.copyOf(requests);
	}

}
