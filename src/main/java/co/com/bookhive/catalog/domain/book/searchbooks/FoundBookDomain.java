package co.com.bookhive.catalog.domain.book.searchbooks;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FoundBookDomain(
		UUID id,
		String title,
		List<String> authors,
		String category,
		String publisher,
		String format,
		int year,
		String isbn,
		int availableCopies,
		int totalCopies) {

	public FoundBookDomain {
		id = UUIDHelper.getDefaultValue(id);
		title = TextHelper.getDefaultValue(title);
		authors = List.copyOf(ObjectHelper.getDefaultValue(authors, List.of()));
		category = TextHelper.getDefaultValue(category);
		publisher = TextHelper.getDefaultValue(publisher);
		format = TextHelper.getDefaultValue(format);
		isbn = TextHelper.getDefaultValue(isbn);
		availableCopies = Math.max(availableCopies, 0);
		totalCopies = Math.max(totalCopies, availableCopies);
	}

	public FoundBookDomain withCopies(final int available, final int total) {
		return new FoundBookDomain(id, title, authors, category, publisher, format, year, isbn, available, total);
	}

}
