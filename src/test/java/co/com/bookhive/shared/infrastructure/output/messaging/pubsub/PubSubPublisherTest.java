package co.com.bookhive.shared.infrastructure.output.messaging.pubsub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.testdata.FakeHttpServer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class PubSubPublisherTest {

	private static final String PUBLISH = "/v1/projects/bookhive-511618/topics/bookhive-avisos:publish";

	@Test
	@DisplayName("publica el mensaje con sus atributos y el pase de la cuenta de servicio")
	void publishesWithAttributesAndToken() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("GET", "/token", 200, "{\"access_token\":\"pase-1\",\"expires_in\":3600}")
					.answer("POST", PUBLISH, 200, "{\"messageIds\":[\"1\"]}");
			final PubSubPublisher publisher = new PubSubPublisher(server.url(), "bookhive-511618",
					new GoogleAccessTokens(server.url() + "/token", Clock.systemUTC()));

			publisher.publish("bookhive-avisos", Map.of("institucion", "uco"), "{\"kind\":\"PRESTAMO_REGISTRADO\"}");

			final FakeHttpServer.Request publish = server.requests().get(1);
			assertEquals("Bearer pase-1", publish.header("Authorization"));
			final JsonNode message = JsonMapper.shared().readTree(publish.body()).path("messages").get(0);
			assertEquals("uco", message.path("attributes").path("institucion").asString());
			assertEquals("{\"kind\":\"PRESTAMO_REGISTRADO\"}", new String(
					Base64.getDecoder().decode(message.path("data").asString()), StandardCharsets.UTF_8));
			assertEquals("Google", server.requests().get(0).header("Metadata-Flavor"));
		}
	}

	@Test
	@DisplayName("sin servidor de metadatos publica sin pase, como en el emulador")
	void publishesWithoutTokenLocally() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("POST", PUBLISH, 200, "{}");
			final PubSubPublisher publisher = new PubSubPublisher(server.url(), "bookhive-511618",
					new GoogleAccessTokens("", Clock.systemUTC()));

			publisher.publish("bookhive-avisos", null, null);

			assertEquals("", server.requests().get(0).header("Authorization"));
		}
	}

	@Test
	@DisplayName("si Pub/Sub rechaza o no responde, avisa que la mensajería no está disponible")
	void reportsFailures() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("POST", PUBLISH, 403, "{\"error\":\"PERMISSION_DENIED\"}");
			final GoogleAccessTokens noToken = new GoogleAccessTokens("", Clock.systemUTC());
			final PubSubPublisher rejected = new PubSubPublisher(server.url(), "bookhive-511618", noToken);
			final PubSubPublisher unreachable = new PubSubPublisher("http://127.0.0.1:1", "bookhive-511618", noToken);
			final Map<String, String> attributes = Map.of();

			final BookHiveException denied = assertThrows(BookHiveException.class,
					() -> rejected.publish("bookhive-avisos", attributes, "{}"));
			final BookHiveException down = assertThrows(BookHiveException.class,
					() -> unreachable.publish("bookhive-avisos", attributes, "{}"));

			assertTrue(denied.getTechnicalMessage().contains("403"));
			assertEquals(MessageKey.MESSAGING_UNAVAILABLE, down.getUserMessageKey());
		}
	}

	@Test
	@DisplayName("sin proyecto o con un tema inválido no intenta publicar")
	void validatesConfiguration() {
		final GoogleAccessTokens noToken = new GoogleAccessTokens("", Clock.systemUTC());
		final PubSubPublisher withoutProject = new PubSubPublisher("https://pubsub.googleapis.com", "", noToken);
		final PubSubPublisher publisher = new PubSubPublisher("https://pubsub.googleapis.com", "bookhive-511618", noToken);
		final Map<String, String> attributes = Map.of();

		assertThrows(BookHiveException.class, () -> withoutProject.publish("bookhive-avisos", attributes, "{}"));
		assertThrows(BookHiveException.class, () -> publisher.publish("../otro", attributes, "{}"));
	}

}
