package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindBookCopyMapper;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindBookCopyInputPort;
import co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.FindBookCopyUseCase;

public final class FindBookCopyInputAdapter implements FindBookCopyInputPort {

	private final FindBookCopyUseCase useCase;
	private final FindBookCopyMapper mapper;

	public FindBookCopyInputAdapter(
			final FindBookCopyUseCase useCase,
			final FindBookCopyMapper mapper) {

		super();
		this.useCase = useCase;
		this.mapper = mapper;
	}

	@Override
	public FindBookCopyResponseDTO execute(final FindBookCopyRequestDTO request) {

		final FindBookCopyDomain search = mapper.toDomain(request);
		final FoundBookCopyDomain found = useCase.execute(search);

		return mapper.toResponse(found);
	}

}
