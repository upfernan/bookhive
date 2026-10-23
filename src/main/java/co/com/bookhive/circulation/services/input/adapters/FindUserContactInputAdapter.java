package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.user.findusercontact.FindUserContactDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindUserContactRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserContactResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserContactInputPort;
import co.com.bookhive.circulation.services.usecase.user.findusercontact.FindUserContactUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class FindUserContactInputAdapter implements FindUserContactInputPort {

	private final FindUserContactUseCase useCase;

	public FindUserContactInputAdapter(final FindUserContactUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public UserContactResponseDTO execute(final FindUserContactRequestDTO request) {
		final FindUserContactRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new FindUserContactRequestDTO(null));
		final FindUserContactDomain user = useCase.execute(new FindUserContactDomain(safeRequest.user(), "", ""));
		return new UserContactResponseDTO(user.user(), user.name(), user.email());
	}

}
