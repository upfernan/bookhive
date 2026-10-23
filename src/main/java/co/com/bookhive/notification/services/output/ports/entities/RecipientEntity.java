package co.com.bookhive.notification.services.output.ports.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record RecipientEntity(String name, String email) {

	public RecipientEntity {
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
	}

	public boolean hasEmail() {
		return !TextHelper.isDefaultValue(email);
	}

}
