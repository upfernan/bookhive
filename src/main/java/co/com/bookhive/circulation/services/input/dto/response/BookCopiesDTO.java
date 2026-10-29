package co.com.bookhive.circulation.services.input.dto.response;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookCopiesDTO(UUID book, int available, int total) {

	public BookCopiesDTO {
		book = UUIDHelper.getDefaultValue(book);
	}

}
