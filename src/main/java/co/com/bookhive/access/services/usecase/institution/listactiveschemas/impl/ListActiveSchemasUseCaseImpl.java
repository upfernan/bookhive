package co.com.bookhive.access.services.usecase.institution.listactiveschemas.impl;

import co.com.bookhive.access.domain.institution.listactiveschemas.ListActiveSchemasDomain;
import co.com.bookhive.access.services.output.ports.repositories.InstitutionRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.services.usecase.institution.listactiveschemas.ListActiveSchemasUseCase;

public final class ListActiveSchemasUseCaseImpl implements ListActiveSchemasUseCase {

	private final InstitutionRepository institutionRepository;

	public ListActiveSchemasUseCaseImpl(final InstitutionRepository institutionRepository) {
		super();
		this.institutionRepository = institutionRepository;
	}

	@Override
	public ListActiveSchemasDomain execute() {
		return new ListActiveSchemasDomain(institutionRepository.findByCriteria(InstitutionCriteria.active())
				.stream()
				.map(InstitutionEntity::getSchema)
				.toList());
	}

}
