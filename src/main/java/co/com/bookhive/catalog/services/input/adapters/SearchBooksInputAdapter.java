package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.services.input.adapters.mapper.SearchBooksMapper;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SearchBooksInputPort;
import co.com.bookhive.catalog.services.usecase.book.searchbooks.SearchBooksUseCase;

public final class SearchBooksInputAdapter implements SearchBooksInputPort {

	private final SearchBooksUseCase useCase;
	private final SearchBooksMapper mapper;

	public SearchBooksInputAdapter(final SearchBooksUseCase useCase, final SearchBooksMapper mapper) {
		super();
		this.useCase = useCase;
		this.mapper = mapper;
	}

	@Override
	public SearchBooksResponseDTO execute(final SearchBooksRequestDTO request) {
		final FoundBooksDomain found = useCase.execute(mapper.toDomain(request));
		return mapper.toResponse(found);
	}

}
