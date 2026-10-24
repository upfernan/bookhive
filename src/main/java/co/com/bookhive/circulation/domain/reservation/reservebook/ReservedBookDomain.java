package co.com.bookhive.circulation.domain.reservation.reservebook;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReservedBookDomain(UUID reservation, UUID book, String status, int position) {

	public ReservedBookDomain {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		status = TextHelper.getDefaultValue(status);
		position = Math.max(position, 0);
	}

}
