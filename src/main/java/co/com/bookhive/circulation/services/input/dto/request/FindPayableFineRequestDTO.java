package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindPayableFineRequestDTO(UUID fine, UUID user) {

	public FindPayableFineRequestDTO {
		fine = UUIDHelper.getDefaultValue(fine);
		user = UUIDHelper.getDefaultValue(user);
	}

}
