package co.com.bookhive.catalog.services.input.adapters.mapper;

import java.util.List;
import co.com.bookhive.catalog.domain.book.searchbooks.FacetValueDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBookDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FacetValueDTO;
import co.com.bookhive.catalog.services.input.dto.response.FoundBookDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class SearchBooksMapperImpl implements SearchBooksMapper {

	@Override
	public SearchBooksDomain toDomain(final SearchBooksRequestDTO request) {
		final SearchBooksRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new SearchBooksRequestDTO("", "", "", "", 1));
		return new SearchBooksDomain(
				safeRequest.text(),
				safeRequest.author(),
				safeRequest.category(),
				safeRequest.publisher(),
				safeRequest.page());
	}

	@Override
	public SearchBooksResponseDTO toResponse(final FoundBooksDomain domain) {
		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso SearchBooks devolvió un dominio nulo.");
		}
		return new SearchBooksResponseDTO(
				domain.found(),
				domain.page(),
				domain.books().stream().map(SearchBooksMapperImpl::toBook).toList(),
				toFacet(domain.authors()),
				toFacet(domain.categories()),
				toFacet(domain.publishers()));
	}

	private static FoundBookDTO toBook(final FoundBookDomain book) {
		return new FoundBookDTO(
				book.id(),
				book.title(),
				book.authors(),
				book.category(),
				book.publisher(),
				book.format(),
				book.year(),
				book.isbn(),
				book.availableCopies(),
				book.totalCopies());
	}

	private static List<FacetValueDTO> toFacet(final List<FacetValueDomain> values) {
		return values.stream().map(value -> new FacetValueDTO(value.value(), value.count())).toList();
	}

}
