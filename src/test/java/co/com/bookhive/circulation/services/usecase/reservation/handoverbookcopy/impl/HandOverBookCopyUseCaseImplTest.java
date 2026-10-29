package co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandedOverBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class HandOverBookCopyUseCaseImplTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID BOOK_COPY = UUID.randomUUID();
	private static final UUID FIRST_READER = UUID.randomUUID();
	private static final UUID SECOND_READER = UUID.randomUUID();
	private static final Instant MORNING = Instant.parse("2026-10-20T10:00:00Z");

	private FakeRepositories.Reservations reservations;
	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Users users;
	private FakeRepositories.Parameters parameters;
	private List<NotificationRequestEntity> notices;
	private ReservationEntity first;
	private ReservationEntity second;

	@BeforeEach
	void prepare() {
		first = FakeRepositories.reservation(BOOK, FIRST_READER, ReservationEntity.STATUS_WAITING, MORNING);
		second = FakeRepositories.reservation(BOOK, SECOND_READER, ReservationEntity.STATUS_WAITING, MORNING.plusSeconds(60));
		reservations = new FakeRepositories.Reservations()
				.with(second)
				.with(FakeRepositories.reservation(UUID.randomUUID(), UUID.randomUUID(), ReservationEntity.STATUS_WAITING,
						MORNING.minusSeconds(600)))
				.with(first);
		final BookCopyEntity copy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_AVAILABLE, true);
		copy.setBook(BOOK);
		bookCopies = new FakeRepositories.BookCopies().with(copy);
		users = new FakeRepositories.Users()
				.with(FakeRepositories.user(FIRST_READER, "Ana Pérez", "ana@uco.edu.co"))
				.with(FakeRepositories.user(SECOND_READER, "", "luis@uco.edu.co"));
		parameters = new FakeRepositories.Parameters().with(15, 3);
		notices = new ArrayList<>();
	}

	@Test
	@DisplayName("aparta el ejemplar para el primero de la fila, con plazo para recogerlo, y le avisa")
	void handsCopyToFirstInQueue() {
		final LocalDate deadline = today().plusDays(2);

		final HandedOverBookCopyDomain handedOver = useCase(2).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertTrue(handedOver.isHandedOver());
		assertEquals(first.getId(), handedOver.reservation());
		assertEquals(FIRST_READER, handedOver.user());
		assertEquals("Ana Pérez", handedOver.userName());
		assertEquals(deadline, handedOver.pickupDeadline());
		assertEquals(ReservationEntity.STATUS_ASSIGNED, first.getStatus());
		assertEquals(BOOK_COPY, first.getBookCopy());
		assertEquals(deadline, first.getPickupDeadline());
		assertEquals(ReservationEntity.STATUS_WAITING, second.getStatus());
		assertEquals(List.of(BOOK_COPY + ":DISPONIBLE->RESERVADO"), bookCopies.statusChanges());
		assertEquals(List.of(new NotificationRequestEntity(
				HandOverBookCopyDomain.NOTICE_KIND, FIRST_READER, BOOK, first.getId(), deadline)), notices);
	}

	@Test
	@DisplayName("si otra petición ya tomó o canceló al primero, sigue con el siguiente de la fila")
	void skipsReservationTakenByOtherRequest() {
		reservations.takenByOtherRequest(first.getId());

		final HandedOverBookCopyDomain handedOver = useCase(2).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertEquals(second.getId(), handedOver.reservation());
		assertEquals("luis@uco.edu.co", handedOver.userName());
		assertEquals(ReservationEntity.STATUS_WAITING, first.getStatus());
	}

	@Test
	@DisplayName("sin nadie en la fila el ejemplar sigue disponible y no se avisa a nadie")
	void withoutQueueCopyStaysAvailable() {
		reservations = new FakeRepositories.Reservations();

		final HandedOverBookCopyDomain handedOver = useCase(2).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertFalse(handedOver.isHandedOver());
		assertEquals(HandedOverBookCopyDomain.nobody(), handedOver);
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("si el lector ya no existe, el nombre para apartarlo es el código de la reserva")
	void unknownReaderFallsBackToReservationId() {
		users = new FakeRepositories.Users();

		final HandedOverBookCopyDomain handedOver = useCase(2).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertEquals(String.valueOf(first.getId()), handedOver.userName());
	}

	@Test
	@DisplayName("si otra petición ya se llevó el ejemplar, no lo aparta para nadie ni toca la fila")
	void copyTakenByOtherRequestGoesToNobody() {
		bookCopies.changedByOtherRequest();

		final HandedOverBookCopyDomain handedOver = useCase(2).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertFalse(handedOver.isHandedOver());
		assertEquals(ReservationEntity.STATUS_WAITING, first.getStatus());
		assertEquals(ReservationEntity.STATUS_WAITING, second.getStatus());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("si otras peticiones tomaron a todos los de la fila, el ejemplar vuelve a quedar disponible")
	void copyGoesBackWhenNobodyCanTakeIt() {
		reservations.takenByOtherRequest(first.getId()).takenByOtherRequest(second.getId());

		final HandedOverBookCopyDomain handedOver = useCase(2).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertFalse(handedOver.isHandedOver());
		assertEquals(List.of(BOOK_COPY + ":DISPONIBLE->RESERVADO", BOOK_COPY + ":RESERVADO->DISPONIBLE"),
				bookCopies.statusChanges());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("el plazo para recoger es de al menos un día")
	void pickupDaysAreAtLeastOne() {
		final HandedOverBookCopyDomain handedOver = useCase(0).execute(new HandOverBookCopyDomain(BOOK_COPY, BOOK));

		assertEquals(today().plusDays(1), handedOver.pickupDeadline());
	}

	@Test
	@DisplayName("sin ejemplar, sin libro o sin dependencias se rechaza como error del programa")
	void rejectsMissingDataAndDependencies() {
		final HandOverBookCopyUseCaseImpl useCase = useCase(2);
		final HandOverBookCopyDomain withoutBook = new HandOverBookCopyDomain(BOOK_COPY, null);

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutBook)).getKind());
		assertThrows(BookHiveException.class, () -> new HandOverBookCopyUseCaseImpl(
				reservations, bookCopies, users, parameters, null, 2));
	}

	private LocalDate today() {
		return CurrentInstitution.today(CurrentInstitution.parameters(parameters));
	}

	private HandOverBookCopyUseCaseImpl useCase(final int pickupDays) {
		return new HandOverBookCopyUseCaseImpl(reservations, bookCopies, users, parameters, notices::add, pickupDays);
	}

}
