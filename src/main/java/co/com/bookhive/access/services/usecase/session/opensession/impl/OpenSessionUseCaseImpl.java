package co.com.bookhive.access.services.usecase.session.opensession.impl;

import co.com.bookhive.access.domain.session.opensession.OpenSessionDomain;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PersonCriteria;
import co.com.bookhive.access.services.usecase.session.opensession.OpenSessionUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class OpenSessionUseCaseImpl implements OpenSessionUseCase {

	private final IdentityProviderPort identityProvider;
	private final PersonRepository personRepository;

	public OpenSessionUseCaseImpl(
			final IdentityProviderPort identityProvider,
			final PersonRepository personRepository) {

		super();
		this.identityProvider = identityProvider;
		this.personRepository = personRepository;
	}

	@Override
	public OpenSessionDomain execute(final OpenSessionDomain input) {

		final OpenSessionDomain session = ObjectHelper.getDefaultValue(input, new OpenSessionDomain("", ""));
		session.validate();

		final String sessionToken = identityProvider.openSession(session.idToken());
		final String identity = identityProvider.identityOf(sessionToken);

		if (TextHelper.isDefaultValue(identity)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"El pase no trae identificador de persona, y es lo único que se le pide"
							+ " al proveedor de identidad (ADR-028).");
		}

		if (personRepository.findByCriteria(PersonCriteria.activeByIdentity(identity)).isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.SESSION_NOT_RESOLVED,
					"La persona " + identity + " se autenticó pero no está registrada como"
							+ " activa en BookHive, así que no pertenece a ninguna institución.");
		}

		return session.withSessionToken(sessionToken);
	}

}
