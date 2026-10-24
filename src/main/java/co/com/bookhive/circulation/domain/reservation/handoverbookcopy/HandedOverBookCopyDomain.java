package co.com.bookhive.circulation.domain.reservation.handoverbookcopy;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record HandedOverBookCopyDomain(UUID reservation, UUID user, String userName, LocalDate pickupDeadline) {

	public HandedOverBookCopyDomain {
		reservation = UUIDHelper.getDefaultValue(reservation);
		user = UUIDHelper.getDefaultValue(user);
		userName = TextHelper.getDefaultValue(userName);
		pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

	public static HandedOverBookCopyDomain nobody() {
		return new HandedOverBookCopyDomain(null, null, null, null);
	}

	public boolean isHandedOver() {
		return !UUIDHelper.isDefaultValue(reservation);
	}

}
