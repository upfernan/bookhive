package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.permission.authorizeoperation.AuthorizeOperationDomain;
import co.com.bookhive.access.services.input.dto.request.AuthorizeOperationRequestDTO;
import co.com.bookhive.access.services.input.ports.AuthorizeOperationInputPort;
import co.com.bookhive.access.services.usecase.permission.authorizeoperation.AuthorizeOperationUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class AuthorizeOperationInputAdapter implements AuthorizeOperationInputPort {

	private final AuthorizeOperationUseCase useCase;

	public AuthorizeOperationInputAdapter(final AuthorizeOperationUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public void execute(final AuthorizeOperationRequestDTO input) {
		final AuthorizeOperationRequestDTO request =
				ObjectHelper.getDefaultValue(input, new AuthorizeOperationRequestDTO("", ""));
		useCase.execute(new AuthorizeOperationDomain(request.permission(), request.operation()));
	}

}
