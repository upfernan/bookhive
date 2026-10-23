package co.com.bookhive.shared.testdata;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class FakeHttpServer implements AutoCloseable {

	public record Request(String method, String path, Map<String, String> headers, String body) {

		public String header(final String name) {
			return headers.getOrDefault(name.toLowerCase(), "");
		}

	}

	private final HttpServer server;
	private final ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
	private final Map<String, Deque<int[]>> statuses = new HashMap<>();
	private final Map<String, Deque<String>> bodies = new HashMap<>();

	public FakeHttpServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", this::handle);
		server.start();
	}

	public String url() {
		return "http://127.0.0.1:" + server.getAddress().getPort();
	}

	public FakeHttpServer answer(final String method, final String path, final int status, final String body) {
		statuses.computeIfAbsent(method + " " + path, key -> new ArrayDeque<>()).add(new int[] { status });
		bodies.computeIfAbsent(method + " " + path, key -> new ArrayDeque<>()).add(body);
		return this;
	}

	public List<Request> requests() {
		return new ArrayList<>(requests);
	}

	@Override
	public void close() {
		server.stop(0);
	}

	private void handle(final HttpExchange exchange) throws IOException {
		final Map<String, String> headers = new HashMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), String.join(",", values)));
		final String route = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
		requests.add(new Request(
				exchange.getRequestMethod(),
				exchange.getRequestURI().getPath(),
				headers,
				new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));

		final Deque<int[]> status = statuses.get(route);
		final int code = status == null || status.isEmpty() ? 404 : status.poll()[0];
		final Deque<String> body = bodies.get(route);
		final byte[] bytes = (body == null || body.isEmpty() ? "" : body.poll()).getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(code, bytes.length == 0 ? -1 : bytes.length);
		try (OutputStream output = exchange.getResponseBody()) {
			output.write(bytes);
		}
	}

}
