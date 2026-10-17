package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.InstitutionParametersJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParametersJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.PageCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

@Component
public class InstitutionParametersRepositoryAdapter implements InstitutionParametersRepository {

	private final InstitutionParametersJpaRepository repository;
	private final EntityMapper<InstitutionParametersEntity, InstitutionParametersJpaEntity> mapper;

	public InstitutionParametersRepositoryAdapter(
			final InstitutionParametersJpaRepository repository,
			final EntityMapper<InstitutionParametersEntity, InstitutionParametersJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public Optional<InstitutionParametersEntity> findById(final UUID id) {
		return repository.findById(id).map(mapper::toEntity);
	}

	@Override
	public List<InstitutionParametersEntity> findAll(final PageCriteria page) {
		final PageCriteria safePage = ObjectHelper.getDefaultValue(page, PageCriteria.defaultPage());
		return repository.findAll(PageRequest.of(safePage.getPage(), safePage.getSize()))
				.stream().map(mapper::toEntity).toList();
	}

}
