package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.OpenSessionResponseDTO;
import co.com.bookhive.circulation.services.input.ports.OpenSessionInputPort;
import co.com.bookhive.circulation.services.output.ports.IdentityProviderPort;
import co.com.bookhive.circulation.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public class OpenSessionInteractor implements OpenSessionInputPort {

	private final IdentityProviderPort identityProvider;
	private final PersonRepository personRepository;

	public OpenSessionInteractor(
			final IdentityProviderPort identityProvider,
			final PersonRepository personRepository) {

		super();
		this.identityProvider = identityProvider;
		this.personRepository = personRepository;
	}

	@Override
	public OpenSessionResponseDTO execute(final OpenSessionRequestDTO request) {

		if (ObjectHelper.isNull(request) || TextHelper.isDefaultValue(request.getIdToken())) {
			throw BookHiveException.create(
					MessageKey.SESSION_ID_TOKEN_REQUIRED,
					"Se pidió abrir una sesión sin el pase que entrega el proveedor de identidad.");
		}

		final String sessionToken = identityProvider.openSession(request.getIdToken());

		personRepository.findActive(identityProvider.identityOf(sessionToken));

		return new OpenSessionResponseDTO(sessionToken);
	}

}
