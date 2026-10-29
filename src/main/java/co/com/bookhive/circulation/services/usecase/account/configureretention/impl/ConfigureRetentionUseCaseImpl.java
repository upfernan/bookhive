package co.com.bookhive.circulation.services.usecase.account.configureretention.impl;

import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParameterChangeRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserLifecycleRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;
import co.com.bookhive.circulation.services.usecase.account.changeuserstatus.impl.ChangeUserStatusUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.account.configureretention.ConfigureRetentionUseCase;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ConfigureRetentionUseCaseImpl implements ConfigureRetentionUseCase {

	public static final String PARAMETER = "retention_months";

	private final UserLifecycleRepository users;
	private final InstitutionParameterChangeRepository changes;
	private final SessionContextPort session;
	private final AuditPort audit;

	public ConfigureRetentionUseCaseImpl(
			final UserLifecycleRepository users,
			final InstitutionParameterChangeRepository changes,
			final SessionContextPort session,
			final AuditPort audit) {
		super();
		this.users = ChangeUserStatusUseCaseImpl.requireDependency(users, "el repositorio de usuarios");
		this.changes = ChangeUserStatusUseCaseImpl.requireDependency(changes, "el historial de parámetros");
		this.session = ChangeUserStatusUseCaseImpl.requireDependency(session, "el contexto de la sesión");
		this.audit = ChangeUserStatusUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public UserLifecycleDomains.Retention execute(final UserLifecycleDomains.Retention requested) {
		final int current = users.retentionMonths();
		if (requested == null) {
			return new UserLifecycleDomains.Retention(current);
		}
		requested.validate();
		if (requested.months() != current) {
			users.changeRetention(requested.months());
			changes.create(new InstitutionParameterChangeEntity(UUIDHelper.generateNew(), PARAMETER,
					String.valueOf(current), String.valueOf(requested.months()), session.getLibrarian(), null));
			audit.register(AuditPort.INSTITUTION_CONFIGURED, session.getInstitution(), session.getLibrarian(),
					PARAMETER + " " + current + "->" + requested.months());
		}
		return requested;
	}

}
