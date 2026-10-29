package co.com.bookhive.circulation.domain.user.finduser;

import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReadyReservationDomain(
		UUID reservation,
		UUID book,
		UUID bookCopy,
		String barcode,
		LocalDate pickupDeadline) {

	public ReadyReservationDomain {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		barcode = TextHelper.getDefaultValue(barcode);
		pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

}
