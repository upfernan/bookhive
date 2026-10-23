package co.com.bookhive.circulation.services.input.dto.response;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record UserContactResponseDTO(UUID user, String name, String email) {

	public UserContactResponseDTO {
		user = UUIDHelper.getDefaultValue(user);
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
	}

}
