package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReturnBookRequestDTO(UUID bookCopy) {

	public ReturnBookRequestDTO {
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
	}

}
