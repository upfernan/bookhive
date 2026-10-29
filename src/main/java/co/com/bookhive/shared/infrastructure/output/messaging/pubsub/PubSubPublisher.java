package co.com.bookhive.shared.infrastructure.output.messaging.pubsub;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class PubSubPublisher {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
	private static final Pattern TOPIC_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9._~+%-]{2,254}$");
	private static final MediaType JSON_BODY = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

	private final String url;
	private final String project;
	private final GoogleAccessTokens accessTokens;
	private final RestClient client;
	private final JsonMapper json = JsonMapper.shared();

	public PubSubPublisher(
			@Value("${bookhive.messaging.pubsub-url:https://pubsub.googleapis.com}") final String url,
			@Value("${bookhive.messaging.project:}") final String project,
			final GoogleAccessTokens accessTokens) {

		super();
		this.url = TextHelper.getDefaultValue(url);
		this.project = TextHelper.getDefaultValue(project);
		this.accessTokens = accessTokens;
		final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.client = RestClient.builder().requestFactory(requestFactory).build();
	}

	public void publish(final String topic, final Map<String, String> attributes, final String data) {
		if (TextHelper.isDefaultValue(url) || TextHelper.isDefaultValue(project)) {
			throw BookHiveException.createUnexpected(
					MessageKey.MESSAGING_UNAVAILABLE,
					"Falta bookhive.messaging.project: no hay proyecto de Pub/Sub configurado (ADR-034).");
		}
		if (!TOPIC_NAME.matcher(TextHelper.getDefaultValue(topic)).matches()) {
			throw BookHiveException.createUnexpected(
					MessageKey.MESSAGING_UNAVAILABLE,
					"El tema \"" + topic + "\" no tiene un nombre válido de Pub/Sub.");
		}

		final ObjectNode message = json.createObjectNode();
		message.put("data", Base64.getEncoder().encodeToString(
				TextHelper.getDefaultValue(data).getBytes(StandardCharsets.UTF_8)));
		final ObjectNode attributeNode = message.putObject("attributes");
		ObjectHelper.getDefaultValue(attributes, Map.<String, String>of()).forEach(attributeNode::put);
		final ObjectNode body = json.createObjectNode();
		body.putArray("messages").add(message);

		try {
			final RestClient.RequestBodySpec request = client.post()
					.uri(url + "/v1/projects/" + project + "/topics/" + topic + ":publish")
					.contentType(JSON_BODY);
			if (accessTokens.isRequired()) {
				request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokens.current());
			}
			final int status = request.body(json.writeValueAsString(body))
					.exchange((sent, response) -> response.getStatusCode().value());
			if (status < 200 || status >= 300) {
				throw BookHiveException.createUnexpected(
						MessageKey.MESSAGING_UNAVAILABLE,
						"Pub/Sub respondió " + status + " al publicar en el tema " + topic + ".");
			}
		} catch (final RestClientException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.MESSAGING_UNAVAILABLE,
					"Pub/Sub no respondió al publicar en el tema " + topic + " dentro del tiempo máximo.");
		}
	}

}
