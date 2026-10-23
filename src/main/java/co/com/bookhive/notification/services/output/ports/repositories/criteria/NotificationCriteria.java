package co.com.bookhive.notification.services.output.ports.repositories.criteria;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record NotificationCriteria(String idempotencyKey) {

	public NotificationCriteria {
		idempotencyKey = TextHelper.getDefaultValue(idempotencyKey);
	}

	public static NotificationCriteria byKey(final String idempotencyKey) {
		return new NotificationCriteria(idempotencyKey);
	}

}
