package co.com.bookhive.access.services.usecase.session.resolvesession.impl;

import java.util.List;

import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.output.ports.SessionIdentityPort;
import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PersonCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ResolveSessionUseCaseImpl implements ResolveSessionUseCase {

	private final SessionIdentityPort sessionIdentity;
	private final PersonRepository personRepository;

	public ResolveSessionUseCaseImpl(
			final SessionIdentityPort sessionIdentity,
			final PersonRepository personRepository) {

		super();
		this.sessionIdentity = sessionIdentity;
		this.personRepository = personRepository;
	}

	@Override
	public ResolveSessionDomain execute() {

		final String identity = sessionIdentity.getIdentity();

		if (TextHelper.isDefaultValue(identity)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"La petición llegó sin identificador de persona en su pase (ADR-028).");
		}

		final List<PersonEntity> people = personRepository.findByCriteria(PersonCriteria.activeByIdentity(identity));

		if (people.isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.SESSION_NOT_RESOLVED,
					"La persona " + identity + " se autenticó pero no está registrada como"
							+ " activa en BookHive, así que no pertenece a ninguna institución.");
		}

		final PersonEntity person = people.get(0);
		return new ResolveSessionDomain(person.getId(), person.getInstitution());
	}

}
