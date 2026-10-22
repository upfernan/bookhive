package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LoanJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

import jakarta.persistence.criteria.Predicate;

@Component
public class LoanRepositoryAdapter implements LoanRepository {

	private static final String ACTIVE_LOAN_INDEX = "uq_loan_book_copy_active";
	private static final String UNIQUE_VIOLATION = "23505";

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
		try {
			repository.saveAndFlush(mapper.toJpaEntity(entity));
		} catch (final RuntimeException exception) {
			if (isActiveLoanCollision(exception)) {
				throw BookHiveException.createConflict(
						MessageKey.BOOK_COPY_NOT_AVAILABLE,
						"Dos préstamos simultáneos del mismo ejemplar: la base de datos rechazó el segundo (RT-01).");
			}
			throw exception;
		}
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

			if (criteria.filtersByStatus()) {
				predicates.add(builder.equal(root.get("status"), criteria.getStatus()));
			}

			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

	private static boolean isActiveLoanCollision(final Throwable exception) {

		Throwable current = exception;

		while (!ObjectHelper.isNull(current)) {

			if (current instanceof ConstraintViolationException violation
					&& mentionsActiveLoanIndex(violation.getConstraintName())) {
				return true;
			}

			if (current instanceof SQLException sql
					&& UNIQUE_VIOLATION.equals(sql.getSQLState())
					&& mentionsActiveLoanIndex(sql.getMessage())) {
				return true;
			}

			current = current.getCause();
		}

		return false;
	}

	private static boolean mentionsActiveLoanIndex(final String text) {
		return TextHelper.getDefaultValue(text).toLowerCase(Locale.ROOT).contains(ACTIVE_LOAN_INDEX);
	}

}
