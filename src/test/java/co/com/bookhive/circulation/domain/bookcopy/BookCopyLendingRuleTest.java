package co.com.bookhive.circulation.domain.bookcopy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class BookCopyLendingRuleTest {

	private static final UUID BOOK_COPY = UUID.randomUUID();
	private static final UUID HOLDER = UUID.randomUUID();

	@Test
	@DisplayName("un ejemplar disponible no se presta: el préstamo solo sale de una reserva")
	void availableCopyNeedsAReservation() {
		final BookCopyEntity copy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_AVAILABLE, true);

		assertEquals(Optional.of(MessageKey.BOOK_COPY_NOT_RESERVED_FOR_USER),
				BookCopyLendingRule.blockFor(copy, HOLDER, null));
	}

	@Test
	@DisplayName("un ejemplar prestado o dado de baja no se presta, sin mirar las reservas")
	void lentOrWithdrawnCopyIsNotAvailable() {
		final BookCopyEntity lent = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_LENT, true);
		final BookCopyEntity withdrawn = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_WITHDRAWN, true);

		assertEquals(Optional.of(MessageKey.BOOK_COPY_NOT_AVAILABLE), BookCopyLendingRule.blockFor(lent, HOLDER, null));
		assertEquals(Optional.of(MessageKey.BOOK_COPY_NOT_AVAILABLE),
				BookCopyLendingRule.blockFor(withdrawn, HOLDER, null));
	}

	@Test
	@DisplayName("un ejemplar apartado solo se presta a quien lo reservó")
	void reservedCopyIsOnlyForItsHolder() {
		final BookCopyEntity copy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_RESERVED, true);
		final ReservationEntity held = FakeRepositories.reservation(
				UUID.randomUUID(), HOLDER, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		held.setBookCopy(BOOK_COPY);
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations().with(held);

		assertTrue(BookCopyLendingRule.blockFor(copy, HOLDER, reservations).isEmpty());
		assertEquals(Optional.of(MessageKey.BOOK_COPY_RESERVED),
				BookCopyLendingRule.blockFor(copy, UUID.randomUUID(), reservations));
		assertEquals(Optional.of(MessageKey.BOOK_COPY_RESERVED),
				BookCopyLendingRule.blockFor(copy, HOLDER, new FakeRepositories.Reservations()));
	}

	@Test
	@DisplayName("para un ejemplar apartado, no recibir las reservas es un error del programa")
	void reservedCopyNeedsReservations() {
		final BookCopyEntity copy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_RESERVED, true);

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> BookCopyLendingRule.blockFor(copy, HOLDER, null)).getKind());
	}

}
