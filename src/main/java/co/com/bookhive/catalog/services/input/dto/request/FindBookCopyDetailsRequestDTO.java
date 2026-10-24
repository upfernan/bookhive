package co.com.bookhive.catalog.services.input.dto.request;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindBookCopyDetailsRequestDTO(String barcode, UUID user) {

	public FindBookCopyDetailsRequestDTO {
		barcode = TextHelper.getDefaultValue(barcode);
		user = UUIDHelper.getDefaultValue(user);
	}

	public FindBookCopyDetailsRequestDTO(final String barcode) {
		this(barcode, null);
	}

}
