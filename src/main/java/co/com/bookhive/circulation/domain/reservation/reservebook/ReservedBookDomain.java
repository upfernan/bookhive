package co.com.bookhive.circulation.domain.reservation.reservebook;

import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReservedBookDomain(UUID reservation, UUID book, String status, int position, LocalDate pickupDeadline) {

	public ReservedBookDomain {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		status = TextHelper.getDefaultValue(status);
		position = Math.max(position, 0);
		pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

}
