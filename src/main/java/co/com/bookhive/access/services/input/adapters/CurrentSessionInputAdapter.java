package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;

public final class CurrentSessionInputAdapter implements CurrentSessionInputPort {

	private final ResolveSessionUseCase useCase;

	public CurrentSessionInputAdapter(final ResolveSessionUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public CurrentSessionResponseDTO execute() {
		final ResolveSessionDomain session = useCase.execute();
		return new CurrentSessionResponseDTO(session.institution(), session.person());
	}

}
