package co.com.bookhive.catalog.domain.book.searchbooks;

import java.util.List;

import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record FoundBooksDomain(
		long found,
		int page,
		List<FoundBookDomain> books,
		List<FacetValueDomain> authors,
		List<FacetValueDomain> categories,
		List<FacetValueDomain> publishers) {

	public FoundBooksDomain {
		found = Math.max(found, 0);
		books = List.copyOf(ObjectHelper.getDefaultValue(books, List.of()));
		authors = List.copyOf(ObjectHelper.getDefaultValue(authors, List.of()));
		categories = List.copyOf(ObjectHelper.getDefaultValue(categories, List.of()));
		publishers = List.copyOf(ObjectHelper.getDefaultValue(publishers, List.of()));
	}

}
