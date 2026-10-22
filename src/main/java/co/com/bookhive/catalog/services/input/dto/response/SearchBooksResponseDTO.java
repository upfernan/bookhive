package co.com.bookhive.catalog.services.input.dto.response;

import java.util.List;

import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record SearchBooksResponseDTO(
		long found,
		int page,
		List<FoundBookDTO> books,
		List<FacetValueDTO> authors,
		List<FacetValueDTO> categories,
		List<FacetValueDTO> publishers) {

	public SearchBooksResponseDTO {
		books = List.copyOf(ObjectHelper.getDefaultValue(books, List.of()));
		authors = List.copyOf(ObjectHelper.getDefaultValue(authors, List.of()));
		categories = List.copyOf(ObjectHelper.getDefaultValue(categories, List.of()));
		publishers = List.copyOf(ObjectHelper.getDefaultValue(publishers, List.of()));
	}

}
