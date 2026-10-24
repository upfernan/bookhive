package co.com.bookhive.catalog.services.output.ports.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record UserReservationEntity(
		UUID reservation,
		UUID book,
		String status,
		int position,
		Instant requestedAt,
		LocalDate pickupDeadline) {

	public UserReservationEntity {
		reservation = UUIDHelper.getDefaultValue(reservation);
		book = UUIDHelper.getDefaultValue(book);
		status = TextHelper.getDefaultValue(status);
		position = Math.max(position, 0);
		requestedAt = DateHelper.getDefaultValue(requestedAt);
		pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

}
