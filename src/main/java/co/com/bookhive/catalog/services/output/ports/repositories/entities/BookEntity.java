package co.com.bookhive.catalog.services.output.ports.repositories.entities;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookEntity(
		UUID id,
		String title,
		String isbn,
		int year,
		String format,
		String status,
		String category,
		String publisher,
		List<String> authors) {

	public static final String STATUS_ACTIVE = "ACTIVO";

	public BookEntity {
		id = UUIDHelper.getDefaultValue(id);
		title = TextHelper.getDefaultValue(title);
		isbn = TextHelper.getDefaultValue(isbn);
		year = Math.max(year, 0);
		format = TextHelper.getDefaultValue(format);
		status = TextHelper.getDefaultValue(status);
		category = TextHelper.getDefaultValue(category);
		publisher = TextHelper.getDefaultValue(publisher);
		authors = List.copyOf(ObjectHelper.getDefaultValue(authors, List.of()));
	}

	public boolean isActive() {
		return STATUS_ACTIVE.equals(status);
	}

}
