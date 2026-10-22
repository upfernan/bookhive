package co.com.bookhive.catalog.services.output.ports.repositories.entities;

import java.util.List;

import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record BookSearchPageEntity(
		long found,
		List<BookEntity> books,
		List<FacetCountEntity> authors,
		List<FacetCountEntity> categories,
		List<FacetCountEntity> publishers) {

	public BookSearchPageEntity {
		found = Math.max(found, 0);
		books = List.copyOf(ObjectHelper.getDefaultValue(books, List.of()));
		authors = List.copyOf(ObjectHelper.getDefaultValue(authors, List.of()));
		categories = List.copyOf(ObjectHelper.getDefaultValue(categories, List.of()));
		publishers = List.copyOf(ObjectHelper.getDefaultValue(publishers, List.of()));
	}

}
