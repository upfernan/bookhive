package co.com.bookhive.catalog.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindBookTitleRequestDTO(UUID book) {

	public FindBookTitleRequestDTO {
		book = UUIDHelper.getDefaultValue(book);
	}

}
