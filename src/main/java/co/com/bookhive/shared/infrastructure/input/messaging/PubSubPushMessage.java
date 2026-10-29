package co.com.bookhive.shared.infrastructure.input.messaging;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record PubSubPushMessage(Message message, String subscription) {

	public PubSubPushMessage {
		message = ObjectHelper.getDefaultValue(message, new Message("", Map.of(), ""));
		subscription = TextHelper.getDefaultValue(subscription);
	}

	public String attribute(final String name) {
		return TextHelper.getDefaultValue(message.attributes().get(name));
	}

	public String text() {
		try {
			return new String(Base64.getDecoder().decode(message.data()), StandardCharsets.UTF_8);
		} catch (final IllegalArgumentException _) {
			return "";
		}
	}

	public String messageId() {
		return message.messageId();
	}

	public record Message(String data, Map<String, String> attributes, String messageId) {

		public Message {
			data = TextHelper.getDefaultValue(data);
			attributes = Map.copyOf(ObjectHelper.getDefaultValue(attributes, Map.of()));
			messageId = TextHelper.getDefaultValue(messageId);
		}

	}

}
