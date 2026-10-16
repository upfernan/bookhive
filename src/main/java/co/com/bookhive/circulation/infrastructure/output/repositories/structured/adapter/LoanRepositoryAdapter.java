package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;

import jakarta.persistence.criteria.Predicate;

@Component
public class LoanRepositoryAdapter implements LoanRepository {

	private final LoanJpaRepository repository;
	private final EntityMapper<LoanEntity, LoanJpaEntity> mapper;

	public LoanRepositoryAdapter(
			final LoanJpaRepository repository,
			final EntityMapper<LoanEntity, LoanJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void create(final LoanEntity entity) {
		repository.save(mapper.toJpaEntity(entity));
	}

	@Override
	public Optional<LoanEntity> findById(final UUID id) {
		return repository.findById(id).map(mapper::toEntity);
	}

	@Override
	public List<LoanEntity> findAll() {
		return repository.findAll().stream().map(mapper::toEntity).toList();
	}

	@Override
	public List<LoanEntity> findByCriteria(final LoanCriteria criteria) {
		return repository.findAll(specificationOf(criteria))
				.stream().map(mapper::toEntity).toList();
	}

	private Specification<LoanJpaEntity> specificationOf(final LoanCriteria criteria) {

		return (root, query, builder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			if (criteria.filtersByUser()) {
				predicates.add(builder.equal(root.get("user"), criteria.getUser()));
			}

			if (criteria.filtersByStatus()) {
				predicates.add(builder.equal(root.get("status"), criteria.getStatus()));
			}

			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
