package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class BookCopyCriteria {

	private final List<UUID> books;
	private final String barcode;
	private final String excludedStatus;

	private BookCopyCriteria(final List<UUID> books, final String barcode, final String excludedStatus) {
		super();
		this.books = List.copyOf(ObjectHelper.getDefaultValue(books, List.of()));
		this.barcode = TextHelper.getDefaultValue(barcode);
		this.excludedStatus = TextHelper.getDefaultValue(excludedStatus);
	}

	public static BookCopyCriteria inService(final List<UUID> books) {
		return new BookCopyCriteria(books, TextHelper.getDefaultValue(), BookCopyEntity.STATUS_WITHDRAWN);
	}

	public static BookCopyCriteria byBarcode(final String barcode) {
		return new BookCopyCriteria(List.of(), barcode, TextHelper.getDefaultValue());
	}

	public List<UUID> getBooks() {
		return books;
	}

	public String getBarcode() {
		return barcode;
	}

	public String getExcludedStatus() {
		return excludedStatus;
	}

	public boolean filtersByBooks() {
		return !books.isEmpty();
	}

	public boolean filtersByBarcode() {
		return !TextHelper.isDefaultValue(barcode);
	}

	public boolean excludesStatus() {
		return !TextHelper.isDefaultValue(excludedStatus);
	}

}
