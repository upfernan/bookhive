package co.com.bookhive.shared.infrastructure.output.messaging.pubsub;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GoogleAccessTokens {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration RENEW_BEFORE = Duration.ofSeconds(60);

	private final String tokenUrl;
	private final RestClient client;
	private final Clock clock;
	private final ReentrantLock lock = new ReentrantLock();
	private String token = "";
	private Instant expiresAt = Instant.EPOCH;

	@Autowired
	public GoogleAccessTokens(@Value("${bookhive.messaging.token-url:}") final String tokenUrl) {
		this(tokenUrl, Clock.systemUTC());
	}

	GoogleAccessTokens(final String tokenUrl, final Clock clock) {
		super();
		this.tokenUrl = TextHelper.getDefaultValue(tokenUrl);
		this.clock = clock;
		final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.client = RestClient.builder().requestFactory(requestFactory).build();
	}

	public boolean isRequired() {
		return !TextHelper.isDefaultValue(tokenUrl);
	}

	public String current() {
		lock.lock();
		try {
			if (TextHelper.isDefaultValue(token) || !clock.instant().isBefore(expiresAt.minus(RENEW_BEFORE))) {
				renew();
			}
			return token;
		} finally {
			lock.unlock();
		}
	}

	private void renew() {
		try {
			final String body = client.get()
					.uri(tokenUrl)
					.header("Metadata-Flavor", "Google")
					.exchange((request, response) -> {
						if (!response.getStatusCode().is2xxSuccessful()) {
							throw unavailable("El servidor de metadatos respondió " + response.getStatusCode().value() + ".");
						}
						return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
					});
			final JsonNode json = JsonMapper.shared().readTree(body);
			final String accessToken = json.path("access_token").asString("");
			if (TextHelper.isDefaultValue(accessToken)) {
				throw unavailable("El servidor de metadatos no entregó el token de acceso.");
			}
			token = accessToken;
			expiresAt = clock.instant().plusSeconds(json.path("expires_in").asLong(0));
		} catch (final RestClientException | JacksonException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.MESSAGING_UNAVAILABLE,
					"No se pudo obtener el token de acceso de la cuenta de servicio para publicar en Pub/Sub.");
		}
	}

	private static BookHiveException unavailable(final String technicalMessage) {
		return BookHiveException.createUnexpected(MessageKey.MESSAGING_UNAVAILABLE, technicalMessage);
	}

}
