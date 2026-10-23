package co.com.bookhive.catalog.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FindBookCopyDetailsRequestDTO(String barcode) {

	public FindBookCopyDetailsRequestDTO {
		barcode = TextHelper.getDefaultValue(barcode);
	}

}
