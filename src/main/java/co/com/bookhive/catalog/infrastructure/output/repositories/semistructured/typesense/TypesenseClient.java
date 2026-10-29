package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class TypesenseClient {

	static final String API_KEY_HEADER = "X-TYPESENSE-API-KEY";

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final String url;
	private final String adminKey;
	private final RestClient client;

	public TypesenseClient(
			@Value("${bookhive.search.url:}") final String url,
			@Value("${bookhive.search.api-key:}") final String adminKey) {

		super();
		this.url = TextHelper.getDefaultValue(url);
		this.adminKey = TextHelper.getDefaultValue(adminKey);

		final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.client = RestClient.builder().requestFactory(requestFactory).build();
	}

	public boolean isConfigured() {
		return !TextHelper.isDefaultValue(url) && !TextHelper.isDefaultValue(adminKey);
	}

	public String adminKey() {
		return adminKey;
	}

	public TypesenseResponse get(final String path, final Map<String, String> parameters, final String key) {
		return exchange(HttpMethod.GET, path, parameters, key, null, null);
	}

	public TypesenseResponse post(
			final String path,
			final Map<String, String> parameters,
			final String key,
			final String body,
			final MediaType contentType) {

		return exchange(HttpMethod.POST, path, parameters, key, body, contentType);
	}

	public TypesenseResponse delete(final String path, final Map<String, String> parameters, final String key) {
		return exchange(HttpMethod.DELETE, path, parameters, key, null, null);
	}

	private TypesenseResponse exchange(
			final HttpMethod method,
			final String path,
			final Map<String, String> parameters,
			final String key,
			final String body,
			final MediaType contentType) {

		final URI uri = uriOf(path, ObjectHelper.getDefaultValue(parameters, Map.of()));
		try {
			final RestClient.RequestBodySpec request = client.method(method).uri(uri).header(API_KEY_HEADER, key);
			if (!ObjectHelper.isNull(body)) {
				request.contentType(contentType).body(body);
			}
			return request.exchange((sent, response) -> new TypesenseResponse(
					response.getStatusCode().value(),
					new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)));
		} catch (final RestClientException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.SEARCH_UNAVAILABLE,
					"Typesense no respondió a " + method + " " + path + " dentro del tiempo máximo.");
		}
	}

	private URI uriOf(final String path, final Map<String, String> parameters) {
		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url).path(path);
		final Map<String, String> values = new LinkedHashMap<>();
		parameters.forEach((name, value) -> {
			builder.queryParam(name, "{" + name + "}");
			values.put(name, value);
		});
		return builder.encode().buildAndExpand(values).toUri();
	}

}
