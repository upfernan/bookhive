package co.com.bookhive.catalog.services.input.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record MyReservationResponseDTO(
		UUID reservation,
		UUID book,
		String title,
		String authors,
		String status,
		int position,
		Instant requestedAt,
		LocalDate pickupDeadline) {

	public MyReservationResponseDTO {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		title = TextHelper.getDefaultValue(title);
		authors = TextHelper.getDefaultValue(authors);
		status = TextHelper.getDefaultValue(status);
		position = Math.max(position, 0);
		requestedAt = DateHelper.getDefaultValue(requestedAt);
		pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

}
