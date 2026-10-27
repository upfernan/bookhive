package co.com.bookhive.catalog.services.input.dto.request;

import java.util.List;
import java.util.UUID;

public record TitleRequestDTO(
		UUID id,
		String title,
		String isbn,
		Integer year,
		String format,
		UUID category,
		String publisher,
		List<String> authors,
		UUID branch,
		List<String> barcodes) {

	public TitleRequestDTO forId(final UUID titleId) {
		return new TitleRequestDTO(titleId, title, isbn, year, format, category, publisher, authors, branch, barcodes);
	}

}
