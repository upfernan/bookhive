package co.com.bookhive.circulation.services.output.ports.entities;

import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record NotificationRequestEntity(String kind, UUID user, UUID book, UUID loan, LocalDate dueDate) {

	public NotificationRequestEntity {
		kind = TextHelper.getDefaultValue(kind);
		user = UUIDHelper.getDefaultValue(user);
		book = UUIDHelper.getDefaultValue(book);
		loan = UUIDHelper.getDefaultValue(loan);
		dueDate = DateHelper.getDefaultValue(dueDate);
	}

}
