package co.com.bookhive.access.services.usecase.session.resolveschema.impl;

import java.util.List;
import co.com.bookhive.access.domain.session.resolveschema.ResolveSchemaDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.output.ports.repositories.InstitutionRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.services.usecase.session.resolveschema.ResolveSchemaUseCase;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ResolveSchemaUseCaseImpl implements ResolveSchemaUseCase {

	private final ResolveSessionUseCase resolveSession;
	private final InstitutionRepository institutionRepository;

	public ResolveSchemaUseCaseImpl(
			final ResolveSessionUseCase resolveSession,
			final InstitutionRepository institutionRepository) {

		super();
		this.resolveSession = resolveSession;
		this.institutionRepository = institutionRepository;
	}

	@Override
	public ResolveSchemaDomain execute() {

		final ResolveSessionDomain session = resolveSession.execute();
		final List<InstitutionEntity> institutions =
				institutionRepository.findByCriteria(InstitutionCriteria.activeById(session.institution()));

		if (institutions.isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.INSTITUTION_NOT_ACTIVE,
					"La institución " + session.institution() + " no está activa en BookHive.");
		}

		return new ResolveSchemaDomain(session.institution(), institutions.get(0).getSchema());
	}

}
