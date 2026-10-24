package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.ReservationJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.ReservationJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

class ReservationRepositoryAdapterTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID USER = UUID.randomUUID();

	@Test
	@DisplayName("guarda la reserva")
	void createsReservation() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing();
		final ReservationEntity reservation = new ReservationEntity();
		reservation.setBook(BOOK);

		adapter(repository.as(ReservationJpaRepository.class)).create(reservation);

		assertEquals(BOOK, repository.saved().get(0).getBook());
	}

	@Test
	@DisplayName("si llegan al tiempo dos reservas del mismo usuario y libro la segunda se rechaza")
	void secondActiveReservationIsConflict() {
		final ReservationRepositoryAdapter adapter = failingOnSave(new DataIntegrityViolationException("Duplicado.",
				new ConstraintViolationException("Duplicado.", new SQLException("Duplicado.", "23505"),
						ReservationRepositoryAdapter.ONE_ACTIVE_PER_USER_AND_BOOK)));
		final ReservationRepositoryAdapter fromDriver = failingOnSave(new DataIntegrityViolationException("Duplicado.",
				new SQLException("duplicate key value violates unique constraint \"uq_reservation_active_per_user_book\"",
						"23505")));
		final ReservationEntity reservation = new ReservationEntity();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.create(reservation));

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.RESERVATION_ALREADY_ACTIVE, exception.getUserMessageKey());
		assertEquals(MessageKey.RESERVATION_ALREADY_ACTIVE,
				assertThrows(BookHiveException.class, () -> fromDriver.create(reservation)).getUserMessageKey());
	}

	@Test
	@DisplayName("otros errores de la base de datos siguen como inesperados")
	void otherErrorsAreRethrown() {
		final DataIntegrityViolationException otherConstraint = new DataIntegrityViolationException("Otra.",
				new ConstraintViolationException("Otra.", new SQLException("Otra.", "23503"), "fk_reservation_book"));
		final IllegalStateException driverFailure = new IllegalStateException("Falló.",
				new SQLException("uq_reservation_active_per_user_book", "08006"));
		final ReservationEntity reservation = new ReservationEntity();
		final ReservationRepositoryAdapter first = failingOnSave(otherConstraint);
		final ReservationRepositoryAdapter second = failingOnSave(driverFailure);

		assertSame(otherConstraint, assertThrows(DataIntegrityViolationException.class, () -> first.create(reservation)));
		assertSame(driverFailure, assertThrows(IllegalStateException.class, () -> second.create(reservation)));
	}

	@Test
	@DisplayName("busca por identificador y no consulta con uno vacío")
	void findsById() {
		final ReservationJpaEntity stored = new ReservationJpaEntity();
		stored.setBook(BOOK);
		final ReservationRepositoryAdapter adapter =
				adapter(FakeJpaRepository.storing(stored).as(ReservationJpaRepository.class));

		assertEquals(BOOK, adapter.findById(UUID.randomUUID()).orElseThrow().getBook());
		assertTrue(adapter.findById(null).isEmpty());
	}

	@Test
	@DisplayName("lista por páginas en orden de llegada")
	void findsAllInArrivalOrder() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing(new ReservationJpaEntity());

		final ReservationRepositoryAdapter adapter = adapter(repository.as(ReservationJpaRepository.class));

		final List<ReservationEntity> found = adapter.findAll(PageCriteria.of(2, 10));

		assertEquals(1, found.size());
		assertEquals(2, repository.pages().get(0).getPageNumber());
		assertEquals(Sort.by(Sort.Order.asc("requestedAt"), Sort.Order.asc("id")), repository.pages().get(0).getSort());
		assertEquals(1, adapter.findAll(null).size());
		assertEquals(0, repository.pages().get(1).getPageNumber());
	}

	@Test
	@DisplayName("filtra por usuario, libro y estados activos")
	void filtersByUserBookAndStatus() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing(new ReservationJpaEntity());

		adapter(repository.as(ReservationJpaRepository.class))
				.findByCriteria(ReservationCriteria.activeOfUserForBook(USER, BOOK));

		assertEquals(List.of("user=" + USER, "book=" + BOOK, "status en [EN_ESPERA, ASIGNADA]"), repository.filters());
	}

	@Test
	@DisplayName("la fila de un libro filtra solo por libro y por estado en espera")
	void queueFiltersByBook() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing(new ReservationJpaEntity());

		adapter(repository.as(ReservationJpaRepository.class)).findByCriteria(ReservationCriteria.waitingForBook(BOOK));

		assertEquals(List.of("book=" + BOOK, "status en [EN_ESPERA]"), repository.filters());
	}

	@Test
	@DisplayName("la reserva que tiene apartado un ejemplar se busca por el ejemplar y el estado asignada")
	void findsReservationHoldingCopy() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing(new ReservationJpaEntity());
		final UUID bookCopy = UUID.randomUUID();

		adapter(repository.as(ReservationJpaRepository.class))
				.findByCriteria(ReservationCriteria.assignedToBookCopy(bookCopy));

		assertEquals(List.of("bookCopy=" + bookCopy, "status en [ASIGNADA]"), repository.filters());
	}

	@Test
	@DisplayName("las reservas por vencer se buscan por plazo anterior a la fecha y estado asignada")
	void findsAssignedWithPickupBefore() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing(new ReservationJpaEntity());
		final LocalDate today = LocalDate.of(2026, 10, 24);

		adapter(repository.as(ReservationJpaRepository.class))
				.findByCriteria(ReservationCriteria.assignedWithPickupBefore(today));

		assertEquals(List.of("pickupDeadline<" + today, "status en [ASIGNADA]"), repository.filters());
	}

	@Test
	@DisplayName("asigna el ejemplar solo si la reserva sigue en espera y con datos completos")
	void assignsConditionally() {
		final UUID reservation = UUID.randomUUID();
		final UUID bookCopy = UUID.randomUUID();
		final Instant now = Instant.parse("2026-10-24T15:00:00Z");
		final LocalDate deadline = LocalDate.of(2026, 10, 26);
		final FakeJpaRepository<ReservationJpaEntity> assigned = FakeJpaRepository.storing();
		final FakeJpaRepository<ReservationJpaEntity> taken = FakeJpaRepository.<ReservationJpaEntity>storing()
				.changingRows(0);
		final ReservationRepositoryAdapter adapter = adapter(assigned.as(ReservationJpaRepository.class));

		assertTrue(adapter.assign(reservation, bookCopy, now, deadline));
		assertEquals(List.of(reservation + ":EN_ESPERA->ASIGNADA ejemplar " + bookCopy + " hasta " + deadline),
				assigned.filters());
		assertFalse(adapter(taken.as(ReservationJpaRepository.class)).assign(reservation, bookCopy, now, deadline));
		assertFalse(adapter.assign(null, bookCopy, now, deadline));
		assertFalse(adapter.assign(reservation, null, now, deadline));
		assertFalse(adapter.assign(reservation, bookCopy, null, deadline));
		assertFalse(adapter.assign(reservation, bookCopy, now, null));
		assertEquals(1, assigned.filters().size());
	}

	@Test
	@DisplayName("sin usuario ni libro no consulta")
	void skipsIncompleteSearch() {
		final FakeJpaRepository<ReservationJpaEntity> repository = FakeJpaRepository.storing(new ReservationJpaEntity());
		final ReservationRepositoryAdapter adapter = adapter(repository.as(ReservationJpaRepository.class));

		assertTrue(adapter.findByCriteria(ReservationCriteria.activeOfUser(null)).isEmpty());
		assertTrue(adapter.findByCriteria(null).isEmpty());
		assertTrue(repository.filters().isEmpty());
	}

	@Test
	@DisplayName("cambia el estado solo si sigue en el esperado y con datos completos")
	void changesStatusConditionally() {
		final UUID reservation = UUID.randomUUID();
		final FakeJpaRepository<ReservationJpaEntity> changed = FakeJpaRepository.storing();
		final FakeJpaRepository<ReservationJpaEntity> untouched = FakeJpaRepository.<ReservationJpaEntity>storing()
				.changingRows(0);
		final ReservationRepositoryAdapter adapter = adapter(changed.as(ReservationJpaRepository.class));

		assertTrue(adapter.changeStatus(reservation, " EN_ESPERA ", "CANCELADA"));
		assertEquals(List.of(reservation + ":EN_ESPERA->CANCELADA"), changed.filters());
		assertFalse(adapter(untouched.as(ReservationJpaRepository.class))
				.changeStatus(reservation, "EN_ESPERA", "CANCELADA"));
		assertFalse(adapter.changeStatus(null, "EN_ESPERA", "CANCELADA"));
		assertFalse(adapter.changeStatus(reservation, "", "CANCELADA"));
		assertFalse(adapter.changeStatus(reservation, "EN_ESPERA", null));
		assertEquals(1, changed.filters().size());
	}

	private static ReservationRepositoryAdapter failingOnSave(final RuntimeException failure) {
		final ReservationJpaRepository repository = (ReservationJpaRepository) Proxy.newProxyInstance(
				ReservationJpaRepository.class.getClassLoader(),
				new Class<?>[] { ReservationJpaRepository.class },
				(instance, method, arguments) -> {
					throw failure;
				});
		return adapter(repository);
	}

	private static ReservationRepositoryAdapter adapter(final ReservationJpaRepository repository) {
		return new ReservationRepositoryAdapter(repository, new FakeEntityMapper<>(jpa -> {
			final ReservationEntity entity = new ReservationEntity();
			entity.setBook(jpa.getBook());
			return entity;
		}, entity -> {
			final ReservationJpaEntity jpa = new ReservationJpaEntity();
			jpa.setBook(entity.getBook());
			jpa.setUser(entity.getUser());
			jpa.setStatus(entity.getStatus());
			return jpa;
		}));
	}

}
