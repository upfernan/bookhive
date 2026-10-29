package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReserveBookRequestDTO(UUID book) {

	public ReserveBookRequestDTO {
		book = UUIDHelper.getDefaultValue(book);
	}

}
