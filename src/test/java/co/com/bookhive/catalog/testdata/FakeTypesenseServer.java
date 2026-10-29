package co.com.bookhive.catalog.testdata;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class FakeTypesenseServer implements AutoCloseable {

	public record Request(String method, String path, Map<String, String> parameters, String key, String contentType,
			String body) {
	}

	public record Answer(int status, String body) {
	}

	private final HttpServer server;
	private final ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
	private final Map<String, Deque<Answer>> scripted = new HashMap<>();
	private final Map<String, Function<Request, Answer>> handlers = new HashMap<>();

	public FakeTypesenseServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", this::handle);
		server.start();
	}

	public String url() {
		return "http://127.0.0.1:" + server.getAddress().getPort();
	}

	public FakeTypesenseServer answer(final String method, final String path, final int status, final String body) {
		scripted.computeIfAbsent(method + " " + path, key -> new ArrayDeque<>()).add(new Answer(status, body));
		return this;
	}

	public FakeTypesenseServer handle(final String method, final String path, final Function<Request, Answer> handler) {
		handlers.put(method + " " + path, handler);
		return this;
	}

	public List<Request> requests() {
		return new ArrayList<>(requests);
	}

	public List<Request> requests(final String method, final String path) {
		return requests.stream().filter(request -> request.method().equals(method) && request.path().equals(path)).toList();
	}

	@Override
	public void close() {
		server.stop(0);
	}

	private void handle(final HttpExchange exchange) throws IOException {
		final String method = exchange.getRequestMethod();
		final String path = exchange.getRequestURI().getRawPath();
		final Request request = new Request(
				method,
				path,
				parametersOf(exchange.getRequestURI().getRawQuery()),
				exchange.getRequestHeaders().getFirst("X-TYPESENSE-API-KEY"),
				exchange.getRequestHeaders().getFirst("Content-Type"),
				new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
		requests.add(request);

		final String route = method + " " + path;
		final Deque<Answer> queue = scripted.get(route);
		final Answer answer;
		if (queue != null && !queue.isEmpty()) {
			answer = queue.poll();
		} else if (handlers.containsKey(route)) {
			answer = handlers.get(route).apply(request);
		} else {
			answer = new Answer(404, "{\"message\": \"Not Found\"}");
		}

		final byte[] body = answer.body().getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(answer.status(), body.length == 0 ? -1 : body.length);
		try (OutputStream output = exchange.getResponseBody()) {
			output.write(body);
		}
	}

	private static Map<String, String> parametersOf(final String query) {
		final Map<String, String> parameters = new LinkedHashMap<>();
		if (query == null || query.isEmpty()) {
			return parameters;
		}
		for (final String pair : query.split("&")) {
			final int equals = pair.indexOf('=');
			final String name = URLDecoder.decode(equals < 0 ? pair : pair.substring(0, equals), StandardCharsets.UTF_8);
			final String value = equals < 0 ? "" : URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8);
			parameters.put(name, value);
		}
		return parameters;
	}

}
