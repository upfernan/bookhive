package co.com.bookhive.notification.services.output.ports.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record EmailEntity(String toEmail, String toName, String subject, String text) {

	public EmailEntity {
		toEmail = TextHelper.getDefaultValue(toEmail);
		toName = TextHelper.getDefaultValue(toName);
		subject = TextHelper.getDefaultValue(subject);
		text = TextHelper.getDefaultValue(text);
	}

}
