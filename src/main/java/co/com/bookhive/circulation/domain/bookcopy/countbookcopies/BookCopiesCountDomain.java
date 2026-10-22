package co.com.bookhive.circulation.domain.bookcopy.countbookcopies;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookCopiesCountDomain(UUID book, int available, int total) {

	public BookCopiesCountDomain {
		book = UUIDHelper.getDefaultValue(book);
		available = Math.max(available, 0);
		total = Math.max(total, available);
	}

}
