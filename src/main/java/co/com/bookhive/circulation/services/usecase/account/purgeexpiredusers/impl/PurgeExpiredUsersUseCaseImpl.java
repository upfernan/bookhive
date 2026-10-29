package co.com.bookhive.circulation.services.usecase.account.purgeexpiredusers.impl;

import java.util.UUID;
import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;
import co.com.bookhive.circulation.services.output.ports.PersonAccessPort;
import co.com.bookhive.circulation.services.output.ports.repositories.UserLifecycleRepository;
import co.com.bookhive.circulation.services.usecase.account.changeuserstatus.impl.ChangeUserStatusUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.account.purgeexpiredusers.PurgeExpiredUsersUseCase;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class PurgeExpiredUsersUseCaseImpl implements PurgeExpiredUsersUseCase {

	private final UserLifecycleRepository users;
	private final PersonAccessPort people;
	private final AuditPort audit;

	public PurgeExpiredUsersUseCaseImpl(final UserLifecycleRepository users, final PersonAccessPort people, final AuditPort audit) {
		super();
		this.users = ChangeUserStatusUseCaseImpl.requireDependency(users, "el repositorio de usuarios");
		this.people = ChangeUserStatusUseCaseImpl.requireDependency(people, "el directorio de personas");
		this.audit = ChangeUserStatusUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public UserLifecycleDomains.PurgeRun execute(final String institution) {
		int purged = 0;
		int skipped = 0;
		for (final UUID user : users.expiredInactive(users.retentionMonths())) {
			final boolean locked = users.lock(user).filter(account -> UserLifecycleDomains.RETIRED.equals(account.status())).isPresent();
			if (!locked || users.activeLoans(user) > 0 || users.pendingFines(user) > 0) {
				skipped++;
				continue;
			}
			users.anonymize(user);
			people.anonymize(user);
			audit.register(AuditPort.USER_PURGED, null, user, "plazo de conservación vencido en " + institution);
			purged++;
		}
		return new UserLifecycleDomains.PurgeRun(institution, purged, skipped);
	}

}
