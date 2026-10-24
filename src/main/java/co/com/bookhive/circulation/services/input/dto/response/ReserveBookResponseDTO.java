package co.com.bookhive.circulation.services.input.dto.response;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReserveBookResponseDTO(UUID reservation, UUID book, String status, int position) {

	public ReserveBookResponseDTO {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		status = TextHelper.getDefaultValue(status);
		position = Math.max(position, 0);
	}

}
