package co.com.bookhive.circulation.services.usecase.reservation.reservebook.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.reservation.reservebook.ReserveBookDomain;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReservedBookDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class ReserveBookUseCaseImplTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID READER = UUID.randomUUID();
	private static final UUID INSTITUTION = UUID.randomUUID();

	private FakeRepositories.Users users;
	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Reservations reservations;
	private List<String> audited;
	private UUID sessionUser;

	@BeforeEach
	void prepare() {
		users = new FakeRepositories.Users().with(FakeRepositories.user(READER, "Ana Pérez", "ana@uco.edu.co"));
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopyOf(BOOK, BookCopyEntity.STATUS_LENT))
				.with(FakeRepositories.bookCopyOf(BOOK, BookCopyEntity.STATUS_RESERVED));
		fines = new FakeRepositories.Fines();
		reservations = new FakeRepositories.Reservations();
		audited = new ArrayList<>();
		sessionUser = READER;
	}

	@Test
	@DisplayName("con todos los ejemplares ocupados deja la reserva en espera al final de la fila")
	void reservesAtTheEndOfTheQueue() {
		reservations.with(FakeRepositories.reservation(
				BOOK, UUID.randomUUID(), ReservationEntity.STATUS_WAITING, Instant.parse("2026-10-20T10:00:00Z")));

		final ReservedBookDomain reserved = useCase().execute(new ReserveBookDomain(BOOK, null));

		final ReservationEntity created = reservations.created().get(0);
		assertEquals(BOOK, created.getBook());
		assertEquals(READER, created.getUser());
		assertEquals(ReservationEntity.STATUS_WAITING, created.getStatus());
		assertEquals(created.getId(), reserved.reservation());
		assertEquals(BOOK, reserved.book());
		assertEquals(ReservationEntity.STATUS_WAITING, reserved.status());
		assertEquals(2, reserved.position());
		assertEquals(List.of(AuditPort.RESERVATION_CREATED + " " + INSTITUTION + " " + READER + " "
				+ created.getId() + " libro " + BOOK), audited);
	}

	@Test
	@DisplayName("si hay un ejemplar disponible se presta, no se reserva")
	void rejectsWhenACopyIsAvailable() {
		bookCopies.with(FakeRepositories.bookCopyOf(BOOK, BookCopyEntity.STATUS_AVAILABLE));

		assertRejected(MessageKey.RESERVATION_COPIES_AVAILABLE);
	}

	@Test
	@DisplayName("un libro sin ejemplares que se presten a domicilio no se reserva")
	void rejectsBookWithoutLendableCopies() {
		bookCopies = new FakeRepositories.BookCopies().with(referenceCopy());

		assertRejected(MessageKey.RESERVATION_BOOK_NOT_RESERVABLE);
	}

	@Test
	@DisplayName("un ejemplar de consulta en sala disponible no cuenta como disponible para llevar")
	void referenceCopyDoesNotBlockTheReservation() {
		bookCopies.with(referenceCopy());

		assertEquals(1, useCase().execute(new ReserveBookDomain(BOOK, null)).position());
	}

	@Test
	@DisplayName("el mismo usuario no reserva dos veces el mismo libro")
	void rejectsSecondActiveReservation() {
		reservations.with(FakeRepositories.reservation(BOOK, READER, ReservationEntity.STATUS_ASSIGNED, Instant.now()));

		assertRejected(MessageKey.RESERVATION_ALREADY_ACTIVE);
	}

	@Test
	@DisplayName("una reserva cancelada no impide volver a reservar")
	void cancelledReservationDoesNotCount() {
		reservations.with(FakeRepositories.reservation(BOOK, READER, ReservationEntity.STATUS_CANCELLED, Instant.now()));

		assertEquals(1, useCase().execute(new ReserveBookDomain(BOOK, null)).position());
	}

	@Test
	@DisplayName("el usuario debe existir, estar activo y no tener multas pendientes")
	void appliesTheBorrowerRules() {
		users = new FakeRepositories.Users();
		assertRejected(MessageKey.RESERVATION_USER_NOT_FOUND);

		users = new FakeRepositories.Users().with(userWith(UserEntity.STATUS_SUSPENDED));
		assertRejected(MessageKey.USER_SUSPENDED);

		users = new FakeRepositories.Users().with(userWith(UserEntity.STATUS_RETIRED));
		assertRejected(MessageKey.USER_RETIRED);

		users = new FakeRepositories.Users().with(userWith(UserEntity.STATUS_ACTIVE));
		fines.withPendingFine();
		assertRejected(MessageKey.USER_WITH_PENDING_FINES);
	}

	@Test
	@DisplayName("sin libro, sin dominio o sin usuario en la sesión no reserva")
	void rejectsMissingData() {
		final ReserveBookUseCaseImpl useCase = useCase();
		final ReserveBookDomain withoutBook = new ReserveBookDomain(null, null);

		assertEquals(MessageKey.RESERVATION_BOOK_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutBook)).getUserMessageKey());
		assertEquals(MessageKey.RESERVATION_BOOK_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());

		sessionUser = null;
		final ReserveBookUseCaseImpl withoutUser = useCase();
		final ReserveBookDomain request = new ReserveBookDomain(BOOK, null);
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> withoutUser.execute(request)).getKind());
		assertTrue(reservations.created().isEmpty());
	}

	@Test
	@DisplayName("se rechaza crear el caso de uso sin una dependencia")
	void rejectsMissingDependency() {
		final FakeSession session = new FakeSession();

		assertThrows(BookHiveException.class, () -> new ReserveBookUseCaseImpl(
				users, bookCopies, fines, null, session, this::audit));
	}

	private void assertRejected(final String messageKey) {
		final ReserveBookUseCaseImpl useCase = useCase();
		final ReserveBookDomain request = new ReserveBookDomain(BOOK, null);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(messageKey, exception.getUserMessageKey());
		assertTrue(reservations.created().isEmpty());
		assertTrue(audited.isEmpty());
	}

	private static BookCopyEntity referenceCopy() {
		final BookCopyEntity copy = FakeRepositories.bookCopy(UUID.randomUUID(), BookCopyEntity.STATUS_AVAILABLE, false);
		copy.setBook(BOOK);
		return copy;
	}

	private static UserEntity userWith(final String status) {
		final UserEntity user = FakeRepositories.user(READER, "Ana Pérez", "ana@uco.edu.co");
		user.setStatus(status);
		return user;
	}

	private ReserveBookUseCaseImpl useCase() {
		return new ReserveBookUseCaseImpl(users, bookCopies, fines, reservations, new FakeSession(), this::audit);
	}

	private void audit(final String action, final UUID institution, final UUID person, final String detail) {
		audited.add(action + " " + institution + " " + person + " " + detail);
	}

	private final class FakeSession implements SessionContextPort {

		@Override
		public UUID getInstitution() {
			return INSTITUTION;
		}

		@Override
		public UUID getLibrarian() {
			return sessionUser;
		}

	}

}
