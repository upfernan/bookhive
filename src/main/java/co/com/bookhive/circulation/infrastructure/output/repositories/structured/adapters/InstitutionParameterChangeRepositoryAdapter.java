package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.InstitutionParameterChangeJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParameterChangeJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParameterChangeRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;

@Component
public class InstitutionParameterChangeRepositoryAdapter implements InstitutionParameterChangeRepository {

	private final InstitutionParameterChangeJpaRepository repository;
	private final EntityMapper<InstitutionParameterChangeEntity, InstitutionParameterChangeJpaEntity> mapper;

	public InstitutionParameterChangeRepositoryAdapter(
			final InstitutionParameterChangeJpaRepository repository,
			final EntityMapper<InstitutionParameterChangeEntity, InstitutionParameterChangeJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void create(final InstitutionParameterChangeEntity entity) {
		repository.save(mapper.toJpaEntity(entity));
	}

}
