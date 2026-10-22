package co.com.bookhive.circulation.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FindUserRequestDTO(String email) {

	public FindUserRequestDTO {
		email = TextHelper.getDefaultValue(email);
	}

}
