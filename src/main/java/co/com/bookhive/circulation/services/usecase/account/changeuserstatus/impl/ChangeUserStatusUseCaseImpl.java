package co.com.bookhive.circulation.services.usecase.account.changeuserstatus.impl;

import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;
import co.com.bookhive.circulation.services.output.ports.PersonAccessPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.UserLifecycleRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserLifecycleEntity;
import co.com.bookhive.circulation.services.usecase.account.changeuserstatus.ChangeUserStatusUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ChangeUserStatusUseCaseImpl implements ChangeUserStatusUseCase {

	private static final String THE_USER = "El usuario ";

	private final UserLifecycleRepository users;
	private final PersonAccessPort people;
	private final SessionContextPort session;
	private final AuditPort audit;

	public ChangeUserStatusUseCaseImpl(
			final UserLifecycleRepository users,
			final PersonAccessPort people,
			final SessionContextPort session,
			final AuditPort audit) {
		super();
		this.users = requireDependency(users, "el repositorio de usuarios");
		this.people = requireDependency(people, "el directorio de personas");
		this.session = requireDependency(session, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
	}

	@Override
	public UserLifecycleDomains.Account execute(final UserLifecycleDomains.Request input) {
		final UserLifecycleDomains.Request request = ObjectHelper.getDefaultValue(input, new UserLifecycleDomains.Request(null, null));
		request.validate();
		final UserLifecycleDomains.Account account = accountOf(users.lock(request.user()).orElseThrow(() ->
				BookHiveException.create(MessageKey.USER_NOT_FOUND, "No existe el usuario " + request.user() + ".")));
		if (account.isPurged()) {
			if (UserLifecycleDomains.Change.PURGE == request.change()) {
				people.anonymize(account.user());
				return account;
			}
			throw BookHiveException.createConflict(MessageKey.USER_PURGED, THE_USER + account.user() + " ya fue depurado.");
		}
		switch (request.change()) {
			case DEACTIVATE -> deactivate(account);
			case REACTIVATE -> reactivate(account);
			case PURGE -> purge(account);
		}
		return accountOf(users.lock(account.user()).orElseThrow());
	}

	private void deactivate(final UserLifecycleDomains.Account account) {
		if (!UserLifecycleDomains.RETIRED.equals(account.status())) {
			users.changeStatus(account.user(), UserLifecycleDomains.RETIRED);
		}
		people.deactivate(account.user());
		audit.register(AuditPort.USER_DEACTIVATED, session.getInstitution(), session.getLibrarian(), account.user().toString());
	}

	private void reactivate(final UserLifecycleDomains.Account account) {
		users.changeStatus(account.user(), UserLifecycleDomains.ACTIVE);
		people.reactivate(account.user());
		audit.register(AuditPort.USER_REACTIVATED, session.getInstitution(), session.getLibrarian(), account.user().toString());
	}

	private void purge(final UserLifecycleDomains.Account account) {
		if (users.activeLoans(account.user()) > 0) {
			throw BookHiveException.createConflict(MessageKey.USER_HAS_ACTIVE_LOANS, THE_USER + account.user() + " tiene préstamos.");
		}
		if (users.pendingFines(account.user()) > 0) {
			throw BookHiveException.createConflict(MessageKey.USER_HAS_PENDING_FINES, THE_USER + account.user() + " tiene multas.");
		}
		users.anonymize(account.user());
		people.anonymize(account.user());
		audit.register(AuditPort.USER_PURGED, session.getInstitution(), session.getLibrarian(), account.user().toString());
	}

	public static UserLifecycleDomains.Account accountOf(final UserLifecycleEntity entity) {
		return new UserLifecycleDomains.Account(entity.id(), entity.status(), entity.deactivatedAt());
	}

	public static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED, "Un caso de uso de cuentas se creó sin " + name + ".");
		}
		return dependency;
	}

}
