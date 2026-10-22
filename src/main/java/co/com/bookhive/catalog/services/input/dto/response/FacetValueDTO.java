package co.com.bookhive.catalog.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FacetValueDTO(String value, long count) {

	public FacetValueDTO {
		value = TextHelper.getDefaultValue(value);
	}

}
