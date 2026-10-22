package co.com.bookhive.catalog.services.input.adapters.mapper;

import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;

public interface SearchBooksMapper {

	SearchBooksDomain toDomain(SearchBooksRequestDTO request);

	SearchBooksResponseDTO toResponse(FoundBooksDomain domain);

}
