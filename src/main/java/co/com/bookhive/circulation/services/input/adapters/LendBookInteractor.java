package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.services.usecase.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapper;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;

public class LendBookInteractor implements LendBookInputPort {

	private final LendBookUseCase useCase;
	private final SessionContextPort sessionContext;
	private final LendBookMapper mapper;

	public LendBookInteractor(
			final LendBookUseCase useCase,
			final SessionContextPort sessionContext,
			final LendBookMapper mapper) {

		super();
		this.useCase = useCase;
		this.sessionContext = sessionContext;
		this.mapper = mapper;
	}

	@Override
	public LendBookResponseDTO execute(final LendBookRequestDTO request) {

		final LendBookDomain requestedLoan = mapper.toDomain(
				request,
				sessionContext.getInstitution(),
				sessionContext.getLibrarian());

		final LendBookDomain registeredLoan = useCase.execute(requestedLoan);

		return mapper.toResponse(registeredLoan);
	}

}
