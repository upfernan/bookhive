package co.com.bookhive.access.services.usecase.datapolicy.acceptdatapolicy.impl;

import java.util.List;

import co.com.bookhive.access.domain.datapolicy.acceptdatapolicy.AcceptDataPolicyDomain;
import co.com.bookhive.access.domain.datapolicy.finddatapolicy.DataPolicyDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyAcceptanceRepository;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyEntity;
import co.com.bookhive.access.services.usecase.datapolicy.acceptdatapolicy.AcceptDataPolicyUseCase;
import co.com.bookhive.access.services.usecase.datapolicy.finddatapolicy.impl.FindDataPolicyUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class AcceptDataPolicyUseCaseImpl implements AcceptDataPolicyUseCase {

	private final ResolveSessionUseCase resolveSession;
	private final DataPolicyRepository policies;
	private final DataPolicyAcceptanceRepository acceptances;
	private final AuditPort audit;

	public AcceptDataPolicyUseCaseImpl(
			final ResolveSessionUseCase resolveSession,
			final DataPolicyRepository policies,
			final DataPolicyAcceptanceRepository acceptances,
			final AuditPort audit) {

		super();
		this.resolveSession = resolveSession;
		this.policies = policies;
		this.acceptances = acceptances;
		this.audit = audit;
	}

	@Override
	public DataPolicyDomain execute(final AcceptDataPolicyDomain input) {

		final AcceptDataPolicyDomain acceptance = ObjectHelper.getDefaultValue(input, new AcceptDataPolicyDomain(0));
		acceptance.validate();

		final ResolveSessionDomain session = resolveSession.execute();
		final DataPolicyEntity policy = FindDataPolicyUseCaseImpl.currentPolicy(policies);
		acceptance.requireCurrent(policy.version());

		final DataPolicyAcceptanceCriteria criteria = DataPolicyAcceptanceCriteria.of(session.person(), policy.version());
		List<DataPolicyAcceptanceEntity> found = acceptances.findByCriteria(criteria);

		if (found.isEmpty()) {
			acceptances.create(new DataPolicyAcceptanceEntity(session.person(), policy.version(), null));
			found = acceptances.findByCriteria(criteria);
			audit.record(AuditPort.DATA_POLICY_ACCEPTED, session.institution(), session.person(),
					"versión " + policy.version());
		}

		final DataPolicyAcceptanceEntity accepted = found.stream().findFirst().orElseThrow(() ->
				BookHiveException.createUnexpected(MessageKey.REQUEST_UNEXPECTED,
						"La aceptación de " + session.person() + " no quedó guardada."));

		return new DataPolicyDomain(policy.version(), policy.content(), policy.publishedAt(), accepted.acceptedAt());
	}

}
