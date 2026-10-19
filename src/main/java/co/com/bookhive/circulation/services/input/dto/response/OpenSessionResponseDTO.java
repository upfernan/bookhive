package co.com.bookhive.circulation.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record OpenSessionResponseDTO(String sessionToken) {

	public OpenSessionResponseDTO {
		sessionToken = TextHelper.getDefaultValue(sessionToken);
	}

}
