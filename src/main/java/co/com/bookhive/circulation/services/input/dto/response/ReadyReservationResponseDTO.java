package co.com.bookhive.circulation.services.input.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReadyReservationResponseDTO(
		UUID reservation,
		UUID book,
		UUID bookCopy,
		String barcode,
		LocalDate pickupDeadline) {

	public ReadyReservationResponseDTO {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		barcode = TextHelper.getDefaultValue(barcode);
		pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

}
