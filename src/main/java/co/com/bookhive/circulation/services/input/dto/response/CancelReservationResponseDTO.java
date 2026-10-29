package co.com.bookhive.circulation.services.input.dto.response;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record CancelReservationResponseDTO(UUID reservation, UUID book, String status) {

	public CancelReservationResponseDTO {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		status = TextHelper.getDefaultValue(status);
	}

}
