package co.com.bookhive.notification.infrastructure.output.externalservices.brevo;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.notification.services.output.ports.EmailPort;
import co.com.bookhive.notification.services.output.ports.entities.EmailEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class BrevoEmailAdapter implements EmailPort {

	static final String API_KEY_HEADER = "api-key";

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
	private static final MediaType JSON_BODY = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
	private static final int MAX_DETAIL_LENGTH = 200;

	private final String url;
	private final String apiKey;
	private final String senderEmail;
	private final String senderName;
	private final RestClient client;
	private final JsonMapper json = JsonMapper.shared();

	public BrevoEmailAdapter(
			@Value("${bookhive.email.url:https://api.brevo.com/v3/smtp/email}") final String url,
			@Value("${bookhive.email.api-key:}") final String apiKey,
			@Value("${bookhive.email.sender-email:}") final String senderEmail,
			@Value("${bookhive.email.sender-name:BookHive}") final String senderName) {

		super();
		this.url = TextHelper.getDefaultValue(url);
		this.apiKey = TextHelper.getDefaultValue(apiKey);
		this.senderEmail = TextHelper.getDefaultValue(senderEmail);
		this.senderName = TextHelper.getDefaultValue(senderName);

		final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.client = RestClient.builder().requestFactory(requestFactory).build();
	}

	@Override
	public String send(final EmailEntity email) {
		if (TextHelper.isDefaultValue(url) || TextHelper.isDefaultValue(apiKey) || TextHelper.isDefaultValue(senderEmail)) {
			throw unavailable("Falta bookhive.email.api-key o bookhive.email.sender-email: no hay correo configurado (ADR-037).");
		}

		final ObjectNode body = json.createObjectNode();
		body.putObject("sender").put("name", senderName).put("email", senderEmail);
		final ObjectNode recipient = body.putArray("to").addObject().put("email", email.toEmail());
		if (!TextHelper.isDefaultValue(email.toName())) {
			recipient.put("name", email.toName());
		}
		body.put("subject", email.subject());
		body.put("textContent", email.text());

		try {
			final BrevoResponse response = client.post()
					.uri(url)
					.header(API_KEY_HEADER, apiKey)
					.accept(MediaType.APPLICATION_JSON)
					.contentType(JSON_BODY)
					.body(json.writeValueAsString(body))
					.exchange((sent, received) -> new BrevoResponse(
							received.getStatusCode().value(),
							new String(received.getBody().readAllBytes(), StandardCharsets.UTF_8)));
			if (response.status() < 200 || response.status() >= 300) {
				throw unavailable("Brevo respondió " + response.status() + " al enviar un correo: "
						+ abbreviate(response.body()));
			}
			return messageIdOf(response.body());
		} catch (final RestClientException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.EMAIL_UNAVAILABLE,
					"Brevo no respondió dentro del tiempo máximo al enviar un correo.");
		}
	}

	private String messageIdOf(final String body) {
		try {
			return json.readTree(body).path("messageId").asString("");
		} catch (final JacksonException _) {
			return "";
		}
	}

	private static String abbreviate(final String text) {
		final String safeText = TextHelper.getDefaultValue(text);
		return safeText.length() <= MAX_DETAIL_LENGTH ? safeText : safeText.substring(0, MAX_DETAIL_LENGTH) + "...";
	}

	private static BookHiveException unavailable(final String technicalMessage) {
		return BookHiveException.createUnexpected(MessageKey.EMAIL_UNAVAILABLE, technicalMessage);
	}

	private record BrevoResponse(int status, String body) {
	}

}
