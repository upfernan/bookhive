package co.com.bookhive.access.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record CloseSessionRequestDTO(String sessionToken) {

	public CloseSessionRequestDTO {
		sessionToken = TextHelper.getDefaultValue(sessionToken);
	}

}
