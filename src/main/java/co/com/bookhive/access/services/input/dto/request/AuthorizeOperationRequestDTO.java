package co.com.bookhive.access.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record AuthorizeOperationRequestDTO(String permission, String operation) {

	public AuthorizeOperationRequestDTO {
		permission = TextHelper.getDefaultValue(permission);
		operation = TextHelper.getDefaultValue(operation);
	}

}
