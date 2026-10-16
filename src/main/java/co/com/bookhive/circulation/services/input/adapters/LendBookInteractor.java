package co.com.bookhive.circulation.services.input.adapters;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.com.bookhive.circulation.domain.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.usecase.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapper;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;

@Service
@Transactional(rollbackFor = Exception.class)
public class LendBookInteractor implements LendBookInputPort {

	private final LendBookUseCase useCase;
	private final SessionContextPort sessionContext;

	public LendBookInteractor(
			final LendBookUseCase useCase,
			final SessionContextPort sessionContext) {

		super();
		this.useCase = useCase;
		this.sessionContext = sessionContext;
	}

	@Override
	public LendBookResponseDTO execute(final LendBookRequestDTO request) {

		final LendBookDomain requestedLoan = LendBookMapper.toDomain(
				request,
				sessionContext.getInstitution(),
				sessionContext.getLibrarian());

		final LendBookDomain registeredLoan = useCase.execute(requestedLoan);

		return LendBookMapper.toResponse(registeredLoan);
	}

}
