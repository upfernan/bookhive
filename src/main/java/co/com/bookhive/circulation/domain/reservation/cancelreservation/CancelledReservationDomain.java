package co.com.bookhive.circulation.domain.reservation.cancelreservation;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record CancelledReservationDomain(UUID reservation, UUID book, String status) {

	public CancelledReservationDomain {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		status = TextHelper.getDefaultValue(status);
	}

}
