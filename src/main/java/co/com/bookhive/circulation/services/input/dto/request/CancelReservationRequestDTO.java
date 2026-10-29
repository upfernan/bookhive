package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record CancelReservationRequestDTO(UUID reservation) {

	public CancelReservationRequestDTO {
		reservation = UUIDHelper.getDefaultValue(reservation);
	}

}
