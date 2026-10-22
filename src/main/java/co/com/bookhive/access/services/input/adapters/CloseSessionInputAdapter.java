package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.session.closesession.CloseSessionDomain;
import co.com.bookhive.access.services.input.dto.request.CloseSessionRequestDTO;
import co.com.bookhive.access.services.input.ports.CloseSessionInputPort;
import co.com.bookhive.access.services.usecase.session.closesession.CloseSessionUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class CloseSessionInputAdapter implements CloseSessionInputPort {

	private final CloseSessionUseCase useCase;

	public CloseSessionInputAdapter(final CloseSessionUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public void execute(final CloseSessionRequestDTO input) {
		final CloseSessionRequestDTO request = ObjectHelper.getDefaultValue(input, new CloseSessionRequestDTO(""));
		useCase.execute(new CloseSessionDomain(request.sessionToken()));
	}

}
