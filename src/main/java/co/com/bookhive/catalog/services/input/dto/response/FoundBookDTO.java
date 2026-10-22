package co.com.bookhive.catalog.services.input.dto.response;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FoundBookDTO(
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

	public FoundBookDTO {
		id = UUIDHelper.getDefaultValue(id);
		title = TextHelper.getDefaultValue(title);
		authors = List.copyOf(ObjectHelper.getDefaultValue(authors, List.of()));
		category = TextHelper.getDefaultValue(category);
		publisher = TextHelper.getDefaultValue(publisher);
		format = TextHelper.getDefaultValue(format);
		isbn = TextHelper.getDefaultValue(isbn);
	}

}
