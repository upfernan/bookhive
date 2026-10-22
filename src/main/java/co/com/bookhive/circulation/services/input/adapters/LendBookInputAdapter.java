package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapper;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.LendBookUseCase;

public class LendBookInputAdapter implements LendBookInputPort {

	private final LendBookUseCase useCase;
	private final LendBookMapper mapper;

	public LendBookInputAdapter(
			final LendBookUseCase useCase,
			final LendBookMapper mapper) {

		super();
		this.useCase = useCase;
		this.mapper = mapper;
	}

	@Override
	public LendBookResponseDTO execute(final LendBookRequestDTO request) {

		final LendBookDomain requestedLoan = mapper.toDomain(request);

		final LendBookDomain registeredLoan = useCase.execute(requestedLoan);

		return mapper.toResponse(registeredLoan);
	}

}
