package co.com.bookhive.notification.services.output.ports.repositories.entities;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record NotificationEntity(
		UUID id,
		String idempotencyKey,
		String kind,
		UUID user,
		String channel,
		String status,
		String providerMessageId) {

	public static final String CHANNEL_EMAIL = "CORREO";

	public NotificationEntity {
		id = UUIDHelper.getDefaultValue(id);
		idempotencyKey = TextHelper.getDefaultValue(idempotencyKey);
		kind = TextHelper.getDefaultValue(kind);
		user = UUIDHelper.getDefaultValue(user);
		channel = TextHelper.getDefaultValue(channel);
		status = TextHelper.getDefaultValue(status);
		providerMessageId = TextHelper.getDefaultValue(providerMessageId);
	}

}
