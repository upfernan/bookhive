package co.com.bookhive.circulation.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record SearchReadersRequestDTO(String text) {

	public SearchReadersRequestDTO {
		text = TextHelper.getDefaultValue(text);
	}

}
