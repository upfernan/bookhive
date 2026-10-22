package co.com.bookhive.shared.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record ResolveMessageRequestDTO(String messageKey) {

	public ResolveMessageRequestDTO {
		messageKey = TextHelper.getDefaultValue(messageKey);
	}

}
