package co.com.bookhive.access.services.usecase.datapolicy.finddatapolicy.impl;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.access.domain.datapolicy.finddatapolicy.DataPolicyDomain;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyAcceptanceRepository;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyEntity;
import co.com.bookhive.access.services.usecase.datapolicy.finddatapolicy.FindDataPolicyUseCase;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindDataPolicyUseCaseImpl implements FindDataPolicyUseCase {

	private final ResolveSessionUseCase resolveSession;
	private final DataPolicyRepository policies;
	private final DataPolicyAcceptanceRepository acceptances;

	public FindDataPolicyUseCaseImpl(
			final ResolveSessionUseCase resolveSession,
			final DataPolicyRepository policies,
			final DataPolicyAcceptanceRepository acceptances) {

		super();
		this.resolveSession = resolveSession;
		this.policies = policies;
		this.acceptances = acceptances;
	}

	@Override
	public DataPolicyDomain execute() {

		final UUID person = resolveSession.execute().person();
		final DataPolicyEntity policy = currentPolicy(policies);
		final Instant acceptedAt = acceptances.findByCriteria(DataPolicyAcceptanceCriteria.of(person, policy.version()))
				.stream()
				.map(DataPolicyAcceptanceEntity::acceptedAt)
				.findFirst()
				.orElse(DateHelper.getDefaultTimestamp());

		return new DataPolicyDomain(policy.version(), policy.content(), policy.publishedAt(), acceptedAt);
	}

	public static DataPolicyEntity currentPolicy(final DataPolicyRepository policies) {
		return policies.findCurrent().orElseThrow(() -> BookHiveException.createUnexpected(
				MessageKey.DATA_POLICY_NOT_PUBLISHED, "La institución no tiene ninguna versión de la política de datos."));
	}

}
