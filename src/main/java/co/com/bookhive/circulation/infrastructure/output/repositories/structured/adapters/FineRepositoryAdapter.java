package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.FineJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineSettlementJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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
	public void create(final FineEntity entity) {
		repository.saveAndFlush(mapper.toJpaEntity(entity));
	}

	@Override
	public List<FineEntity> findByCriteria(final FineCriteria criteria) {
		return repository.findAll(specificationOf(criteria))
				.stream().map(mapper::toEntity).toList();
	}

	private Specification<FineJpaEntity> specificationOf(final FineCriteria criteria) {

		return (root, query, builder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			if (criteria.filtersById()) {
				predicates.add(builder.equal(root.get("id"), criteria.getId()));
			}

			if (criteria.filtersByUser()) {
				predicates.add(builder.equal(root.get("user").get("id"), criteria.getUser()));
			}

			if (criteria.filtersByLoan()) {
				predicates.add(builder.equal(root.get("loan").get("id"), criteria.getLoan()));
			}

			if (criteria.onlyPending()) {
				final Subquery<UUID> settled = query.subquery(UUID.class);
				final Root<FineSettlementJpaEntity> settlement = settled.from(FineSettlementJpaEntity.class);
				settled.select(settlement.get("fine"))
						.where(builder.equal(settlement.get("fine"), root.get("id")));
				predicates.add(builder.not(builder.exists(settled)));
			}

			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
