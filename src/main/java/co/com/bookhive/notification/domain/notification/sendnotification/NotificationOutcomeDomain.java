package co.com.bookhive.notification.domain.notification.sendnotification;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record NotificationOutcomeDomain(String status) {

	public static final String SENT = "ENVIADO";
	public static final String ALREADY_SENT = "YA_ENVIADO";
	public static final String WITHOUT_EMAIL = "SIN_CORREO";

	public NotificationOutcomeDomain {
		status = TextHelper.getDefaultValue(status);
	}

}
