package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.catalog.services.input.adapters.mapper.FindBookCopyDetailsMapper;
import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.catalog.services.input.ports.FindBookCopyDetailsInputPort;
import co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.FindBookCopyDetailsUseCase;

public final class FindBookCopyDetailsInputAdapter implements FindBookCopyDetailsInputPort {

	private final FindBookCopyDetailsUseCase useCase;
	private final FindBookCopyDetailsMapper mapper;

	public FindBookCopyDetailsInputAdapter(
			final FindBookCopyDetailsUseCase useCase,
			final FindBookCopyDetailsMapper mapper) {

		super();
		this.useCase = useCase;
		this.mapper = mapper;
	}

	@Override
	public FindBookCopyDetailsResponseDTO execute(final FindBookCopyDetailsRequestDTO request) {

		final FindBookCopyDetailsDomain search = mapper.toDomain(request);
		final BookCopyDetailsDomain found = useCase.execute(search);

		return mapper.toResponse(found);
	}

}
