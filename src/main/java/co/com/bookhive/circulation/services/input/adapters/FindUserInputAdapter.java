package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindUserMapper;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserInputPort;
import co.com.bookhive.circulation.services.usecase.user.finduser.FindUserUseCase;

public final class FindUserInputAdapter implements FindUserInputPort {

	private final FindUserUseCase useCase;
	private final FindUserMapper mapper;

	public FindUserInputAdapter(
			final FindUserUseCase useCase,
			final FindUserMapper mapper) {

		super();
		this.useCase = useCase;
		this.mapper = mapper;
	}

	@Override
	public FindUserResponseDTO execute(final FindUserRequestDTO request) {

		final FindUserDomain search = mapper.toDomain(request);
		final FoundUserDomain found = useCase.execute(search);

		return mapper.toResponse(found);
	}

}
