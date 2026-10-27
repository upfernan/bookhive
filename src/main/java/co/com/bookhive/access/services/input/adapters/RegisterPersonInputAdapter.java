package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.session.registerperson.RegistrationDomain;
import co.com.bookhive.access.services.input.dto.request.RegisterPersonRequestDTO;
import co.com.bookhive.access.services.input.dto.response.RegisterPersonResponseDTO;
import co.com.bookhive.access.services.input.ports.RegisterPersonInputPort;
import co.com.bookhive.access.services.usecase.session.registerperson.RegisterPersonUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class RegisterPersonInputAdapter implements RegisterPersonInputPort {

	private final RegisterPersonUseCase useCase;

	public RegisterPersonInputAdapter(final RegisterPersonUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public RegisterPersonResponseDTO execute(final RegisterPersonRequestDTO request) {
		final RegisterPersonRequestDTO safe = ObjectHelper.getDefaultValue(request, new RegisterPersonRequestDTO(null, null, 0));
		final RegistrationDomain.Registered registered = useCase.execute(new RegistrationDomain(safe.idToken(), safe.name(), safe.policyVersion()));
		return new RegisterPersonResponseDTO(registered.person(), registered.sessionToken());
	}

}
