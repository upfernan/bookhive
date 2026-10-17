package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.FineJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;

import jakarta.persistence.criteria.Predicate;

@Component
public class FineRepositoryAdapter implements FineRepository {

	private final FineJpaRepository repository;
	private final EntityMapper<FineEntity, FineJpaEntity> mapper;

	public FineRepositoryAdapter(
			final FineJpaRepository repository,
			final EntityMapper<FineEntity, FineJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public List<FineEntity> findByCriteria(final FineCriteria criteria) {
		return repository.findAll(specificationOf(criteria))
				.stream().map(mapper::toEntity).toList();
	}

	private Specification<FineJpaEntity> specificationOf(final FineCriteria criteria) {

		return (root, query, builder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			if (criteria.filtersByUser()) {
				predicates.add(builder.equal(root.get("user").get("id"), criteria.getUser()));
			}

			if (criteria.filtersByStatus()) {
				predicates.add(builder.equal(root.get("status"), criteria.getStatus()));
			}

			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
