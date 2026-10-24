package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindBookCopyRequestDTO(String barcode, UUID user) {

	public FindBookCopyRequestDTO {
		barcode = TextHelper.getDefaultValue(barcode);
		user = UUIDHelper.getDefaultValue(user);
	}

	public FindBookCopyRequestDTO(final String barcode) {
		this(barcode, null);
	}

}
