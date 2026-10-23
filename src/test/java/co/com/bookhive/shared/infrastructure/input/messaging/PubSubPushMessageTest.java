package co.com.bookhive.shared.infrastructure.input.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PubSubPushMessageTest {

	@Test
	@DisplayName("lee los atributos y el texto del mensaje que entrega Pub/Sub")
	void readsAttributesAndText() {
		final String data = Base64.getEncoder().encodeToString("{\"kind\":\"x\"}".getBytes(StandardCharsets.UTF_8));
		final PubSubPushMessage push = new PubSubPushMessage(
				new PubSubPushMessage.Message(data, Map.of("institucion", "uco"), "42"), "projects/p/subscriptions/s");

		assertEquals("uco", push.attribute("institucion"));
		assertEquals("", push.attribute("tarea"));
		assertEquals("{\"kind\":\"x\"}", push.text());
		assertEquals("42", push.messageId());
		assertEquals("projects/p/subscriptions/s", push.subscription());
	}

	@Test
	@DisplayName("un mensaje vacío o dañado queda con valores vacíos")
	void emptyOrBrokenMessageIsSafe() {
		final PubSubPushMessage empty = new PubSubPushMessage(null, null);
		final PubSubPushMessage broken = new PubSubPushMessage(new PubSubPushMessage.Message("%%%", null, null), "");

		assertEquals("", empty.text());
		assertEquals("", empty.messageId());
		assertEquals("", empty.subscription());
		assertEquals("", broken.text());
		assertEquals("", broken.attribute("institucion"));
	}

}
