package co.com.bookhive.shared.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record ResolveMessageResponseDTO(String text) {

	public ResolveMessageResponseDTO {
		text = TextHelper.getDefaultValue(text);
	}

}
