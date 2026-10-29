package co.com.bookhive.catalog.domain.book.findbooktitle;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindBookTitleDomain(UUID book, String title) {

	public FindBookTitleDomain {
		book = UUIDHelper.getDefaultValue(book);
		title = TextHelper.getDefaultValue(title);
	}

}
