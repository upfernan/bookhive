package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindUserContactRequestDTO(UUID user) {

	public FindUserContactRequestDTO {
		user = UUIDHelper.getDefaultValue(user);
	}

}
