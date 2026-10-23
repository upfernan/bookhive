package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanReturnJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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
		repository.saveAndFlush(mapper.toJpaEntity(entity));
	}

	@Override
	public Optional<LoanEntity> findById(final UUID id) {
		return repository.findById(id).map(mapper::toEntity);
	}

	@Override
	public List<LoanEntity> findAll(final PageCriteria page) {
		final PageCriteria safePage = ObjectHelper.getDefaultValue(page, PageCriteria.defaultPage());
		return repository.findAll(PageRequest.of(safePage.getPage(), safePage.getSize()))
				.stream().map(mapper::toEntity).toList();
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
				predicates.add(builder.equal(root.get("user").get("id"), criteria.getUser()));
			}

			if (criteria.filtersByDueDate()) {
				predicates.add(builder.equal(root.get("dueDate"), criteria.getDueDate()));
			}

			if (criteria.filtersByBookCopy()) {
				predicates.add(builder.equal(root.get("bookCopy").get("id"), criteria.getBookCopy()));
			}

			if (criteria.onlyOpen()) {
				final Subquery<UUID> returned = query.subquery(UUID.class);
				final Root<LoanReturnJpaEntity> loanReturn = returned.from(LoanReturnJpaEntity.class);
				returned.select(loanReturn.get("loan"))
						.where(builder.equal(loanReturn.get("loan"), root.get("id")));
				predicates.add(builder.not(builder.exists(returned)));
			}

			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
