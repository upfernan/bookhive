package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.ReservationJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.ReservationJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;
import jakarta.persistence.criteria.Predicate;

@Component
public class ReservationRepositoryAdapter implements ReservationRepository {

	static final String ONE_ACTIVE_PER_USER_AND_BOOK = "uq_reservation_active_per_user_book";
	private static final String UNIQUE_VIOLATION = "23505";
	private static final Sort QUEUE_ORDER = Sort.by(Sort.Order.asc("requestedAt"), Sort.Order.asc("id"));

	private final ReservationJpaRepository repository;
	private final EntityMapper<ReservationEntity, ReservationJpaEntity> mapper;

	public ReservationRepositoryAdapter(
			final ReservationJpaRepository repository,
			final EntityMapper<ReservationEntity, ReservationJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void create(final ReservationEntity entity) {
		try {
			repository.saveAndFlush(mapper.toJpaEntity(entity));
		} catch (final RuntimeException exception) {
			if (isSecondActiveReservation(exception)) {
				throw BookHiveException.createConflict(
						MessageKey.RESERVATION_ALREADY_ACTIVE,
						"Dos reservas activas del mismo usuario para el mismo libro: la base de datos rechazó la segunda.");
			}
			throw exception;
		}
	}

	@Override
	public Optional<ReservationEntity> findById(final UUID id) {
		if (UUIDHelper.isDefaultValue(id)) {
			return Optional.empty();
		}
		return repository.findById(id).map(mapper::toEntity);
	}

	@Override
	public List<ReservationEntity> findAll(final PageCriteria page) {
		final PageCriteria safePage = ObjectHelper.getDefaultValue(page, PageCriteria.defaultPage());
		return repository.findAll(PageRequest.of(safePage.getPage(), safePage.getSize(), QUEUE_ORDER))
				.stream().map(mapper::toEntity).toList();
	}

	@Override
	public List<ReservationEntity> findByCriteria(final ReservationCriteria criteria) {
		if (ObjectHelper.isNull(criteria) || !criteria.isComplete()) {
			return List.of();
		}
		return repository.findAll(specificationOf(criteria), QUEUE_ORDER)
				.stream().map(mapper::toEntity).toList();
	}

	@Override
	public boolean changeStatus(final UUID reservation, final String expectedStatus, final String newStatus) {
		if (UUIDHelper.isDefaultValue(reservation)
				|| TextHelper.isDefaultValue(expectedStatus)
				|| TextHelper.isDefaultValue(newStatus)) {
			return false;
		}
		return repository.changeStatus(reservation, expectedStatus.trim(), newStatus.trim()) == 1;
	}

	private static Specification<ReservationJpaEntity> specificationOf(final ReservationCriteria criteria) {
		return (root, query, builder) -> {
			final List<Predicate> predicates = new ArrayList<>();
			if (criteria.filtersByUser()) {
				predicates.add(builder.equal(root.get("user"), criteria.getUser()));
			}
			if (criteria.filtersByBook()) {
				predicates.add(builder.equal(root.get("book"), criteria.getBook()));
			}
			predicates.add(root.get("status").in(criteria.getStatuses()));
			return builder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static boolean isSecondActiveReservation(final Throwable exception) {
		Throwable current = exception;
		while (!ObjectHelper.isNull(current)) {
			if (current instanceof ConstraintViolationException violation
					&& mentionsOneActivePerUserAndBook(violation.getConstraintName())) {
				return true;
			}
			if (current instanceof SQLException sql
					&& UNIQUE_VIOLATION.equals(sql.getSQLState())
					&& mentionsOneActivePerUserAndBook(sql.getMessage())) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static boolean mentionsOneActivePerUserAndBook(final String text) {
		return TextHelper.getDefaultValue(text).toLowerCase(Locale.ROOT).contains(ONE_ACTIVE_PER_USER_AND_BOOK);
	}

}
