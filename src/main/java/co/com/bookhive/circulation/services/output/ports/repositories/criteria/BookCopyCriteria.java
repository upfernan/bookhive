package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class BookCopyCriteria {

	private final List<UUID> books;
	private final String excludedStatus;

	private BookCopyCriteria(final List<UUID> books, final String excludedStatus) {
		super();
		this.books = List.copyOf(ObjectHelper.getDefaultValue(books, List.of()));
		this.excludedStatus = TextHelper.getDefaultValue(excludedStatus);
	}

	public static BookCopyCriteria inService(final List<UUID> books) {
		return new BookCopyCriteria(books, BookCopyEntity.STATUS_WITHDRAWN);
	}

	public List<UUID> getBooks() {
		return books;
	}

	public String getExcludedStatus() {
		return excludedStatus;
	}

	public boolean filtersByBooks() {
		return !books.isEmpty();
	}

	public boolean excludesStatus() {
		return !TextHelper.isDefaultValue(excludedStatus);
	}

}
