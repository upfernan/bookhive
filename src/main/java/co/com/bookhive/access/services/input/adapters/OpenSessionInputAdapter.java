package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.session.opensession.OpenSessionDomain;
import co.com.bookhive.access.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.access.services.input.dto.response.OpenSessionResponseDTO;
import co.com.bookhive.access.services.input.ports.OpenSessionInputPort;
import co.com.bookhive.access.services.usecase.session.opensession.OpenSessionUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class OpenSessionInputAdapter implements OpenSessionInputPort {

	private final OpenSessionUseCase useCase;

	public OpenSessionInputAdapter(final OpenSessionUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public OpenSessionResponseDTO execute(final OpenSessionRequestDTO input) {
		final OpenSessionRequestDTO request = ObjectHelper.getDefaultValue(input, new OpenSessionRequestDTO());
		final OpenSessionDomain session = useCase.execute(new OpenSessionDomain(request.getIdToken(), ""));
		return new OpenSessionResponseDTO(session.getSessionToken());
	}

}
