package co.com.bookhive.payment.infrastructure.output.repositories.structured.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.payment.domain.finepayment.PaymentStatus;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.FinePaymentJpaRepository;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentEventJpaEntity;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentJpaEntity;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Component
public class FinePaymentRepositoryAdapter implements FinePaymentRepository {

	private static final List<String> FINAL_STATUSES = List.of(
			PaymentStatus.APPROVED, PaymentStatus.DECLINED, PaymentStatus.VOIDED, PaymentStatus.ERROR);

	private final FinePaymentJpaRepository repository;
	private final EntityMapper<FinePaymentEntity, FinePaymentJpaEntity> mapper;

	public FinePaymentRepositoryAdapter(
			final FinePaymentJpaRepository repository,
			final EntityMapper<FinePaymentEntity, FinePaymentJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void create(final FinePaymentEntity entity) {
		repository.saveAndFlush(mapper.toJpaEntity(entity));
	}

	@Override
	public List<FinePaymentEntity> findByCriteria(final FinePaymentCriteria criteria) {
		final FinePaymentCriteria safeCriteria = ObjectHelper.getDefaultValue(
				criteria, FinePaymentCriteria.withReference(""));
		return repository.findAll(specificationOf(safeCriteria))
				.stream().map(mapper::toEntity).toList();
	}

	private static Specification<FinePaymentJpaEntity> specificationOf(final FinePaymentCriteria criteria) {

		return (root, query, builder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			if (criteria.filtersByReference()) {
				predicates.add(builder.equal(root.get("reference"), criteria.getReference()));
			}

			if (criteria.filtersByFine()) {
				predicates.add(builder.equal(root.get("fine"), criteria.getFine()));
			}

			if (criteria.filtersByPayer()) {
				predicates.add(builder.equal(root.get("payer"), criteria.getPayer()));
			}

			if (criteria.filtersByExpiration()) {
				predicates.add(builder.greaterThan(root.get("expiresAt"), criteria.getExpiresAfter()));
			}

			if (criteria.filtersByCreation()) {
				predicates.add(builder.between(
						root.get("createdAt"), criteria.getCreatedFrom(), criteria.getCreatedUntil()));
			}

			if (criteria.onlyUnresolved()) {
				final Subquery<UUID> resolved = query.subquery(UUID.class);
				final Root<FinePaymentEventJpaEntity> event = resolved.from(FinePaymentEventJpaEntity.class);
				resolved.select(event.get("payment"))
						.where(
								builder.equal(event.get("payment"), root.get("id")),
								event.get("status").in(FINAL_STATUSES));
				predicates.add(builder.not(builder.exists(resolved)));
			}

			if (predicates.isEmpty()) {
				return builder.disjunction();
			}
			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
