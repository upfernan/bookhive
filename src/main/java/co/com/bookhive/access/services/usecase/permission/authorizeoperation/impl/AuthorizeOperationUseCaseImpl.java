package co.com.bookhive.access.services.usecase.permission.authorizeoperation.impl;

import co.com.bookhive.access.domain.permission.authorizeoperation.AuthorizeOperationDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyAcceptanceRepository;
import co.com.bookhive.access.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PermissionCriteria;
import co.com.bookhive.access.services.usecase.permission.authorizeoperation.AuthorizeOperationUseCase;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class AuthorizeOperationUseCaseImpl implements AuthorizeOperationUseCase {

	private final ResolveSessionUseCase resolveSession;
	private final PermissionRepository permissionRepository;
	private final DataPolicyAcceptanceRepository acceptances;
	private final AuditPort audit;

	public AuthorizeOperationUseCaseImpl(
			final ResolveSessionUseCase resolveSession,
			final PermissionRepository permissionRepository,
			final DataPolicyAcceptanceRepository acceptances,
			final AuditPort audit) {

		super();
		this.resolveSession = resolveSession;
		this.permissionRepository = permissionRepository;
		this.acceptances = acceptances;
		this.audit = audit;
	}

	@Override
	public void execute(final AuthorizeOperationDomain input) {

		final AuthorizeOperationDomain operation = ObjectHelper.getDefaultValue(input, new AuthorizeOperationDomain("", ""));
		operation.validate();

		final ResolveSessionDomain session = resolveSession.execute();

		if (acceptances.findByCriteria(DataPolicyAcceptanceCriteria.currentPolicyOf(session.person())).isEmpty()) {
			throw BookHiveException.createNotAllowed(
					MessageKey.DATA_POLICY_NOT_ACCEPTED,
					"La persona " + session.person() + " no ha aceptado la política de datos vigente.");
		}

		final PermissionCriteria criteria = PermissionCriteria.grantedTo(session.person(), operation.permission());

		if (permissionRepository.findByCriteria(criteria).isEmpty()) {
			audit.register(AuditPort.PERMISSION_DENIED, session.institution(), session.person(),
					operation.permission());
			throw BookHiveException.createNotAllowed(
					MessageKey.PERMISSION_DENIED,
					"La persona en sesión no tiene el permiso " + operation.permission() + ".");
		}
	}

}
