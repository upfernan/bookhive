package co.com.bookhive.circulation.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FindBookCopyRequestDTO(String barcode) {

	public FindBookCopyRequestDTO {
		barcode = TextHelper.getDefaultValue(barcode);
	}

}
