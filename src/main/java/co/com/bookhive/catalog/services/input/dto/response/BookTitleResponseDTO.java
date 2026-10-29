package co.com.bookhive.catalog.services.input.dto.response;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookTitleResponseDTO(UUID book, String title) {

	public BookTitleResponseDTO {
		book = UUIDHelper.getDefaultValue(book);
		title = TextHelper.getDefaultValue(title);
	}

}
