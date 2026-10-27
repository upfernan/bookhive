package co.com.bookhive.circulation.domain.bookcopy.findbookcopy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyDomainTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();
	private static final UUID BOOK = UUIDHelper.generateNew();
	private static final String BARCODE = "UCO-0001";

	@Test
	@DisplayName("un ejemplar disponible sin reserva se encuentra, pero queda bloqueado: solo se presta sobre una reserva")
	void blocksAvailableBookCopyWithoutReservation() {
		final FoundBookCopyDomain found = search(" UCO-0001 ", BookCopyEntity.STATUS_AVAILABLE, true).find();

		assertEquals(BOOK_COPY, found.id());
		assertEquals(BOOK, found.book());
		assertEquals(BARCODE, found.barcode());
		assertFalse(found.available());
		assertTrue(found.lendable());
		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_RESERVED_FOR_USER), found.blocks());
	}

	@Test
	@DisplayName("bloquea el ejemplar sin reserva igual que lo rechaza el registro del préstamo")
	void blocksAvailableCopyLikeLending() {
		final FakeRepositories.BookCopies bookCopies = bookCopies(BookCopyEntity.STATUS_AVAILABLE, true);

		assertEquals(rejectionOfLending(bookCopies),
				new FindBookCopyDomain(BARCODE).withRepositories(bookCopies).find().blocks().get(0));
	}

	@Test
	@DisplayName("un ejemplar prestado queda bloqueado por no estar disponible")
	void blocksLentBookCopy() {
		final FoundBookCopyDomain found = search(BARCODE, BookCopyEntity.STATUS_LENT, true).find();

		assertFalse(found.available());
		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE), found.blocks());
	}

	@Test
	@DisplayName("un ejemplar de consulta en sala queda bloqueado aunque esté apartado para quien lo pide")
	void blocksReferenceOnlyBookCopy() {
		final UUID borrower = UUIDHelper.generateNew();

		final FoundBookCopyDomain found = new FindBookCopyDomain(BARCODE, borrower)
				.withRepositories(bookCopies(BookCopyEntity.STATUS_RESERVED, false), heldFor(borrower))
				.find();

		assertFalse(found.lendable());
		assertEquals(List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY), found.blocks());
	}

	@Test
	@DisplayName("un ejemplar dado de baja se encuentra y muestra que no está disponible")
	void findsWithdrawnBookCopyAsNotAvailable() {
		final FoundBookCopyDomain found = search(BARCODE, BookCopyEntity.STATUS_WITHDRAWN, false).find();

		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE, MessageKey.BOOK_COPY_REFERENCE_ONLY), found.blocks());
	}

	@Test
	@DisplayName("bloquea por no disponible igual que lo rechaza el registro del préstamo")
	void blocksNotAvailableLikeLending() {
		final FakeRepositories.BookCopies bookCopies = bookCopies(BookCopyEntity.STATUS_LENT, true);

		assertEquals(rejectionOfLending(bookCopies),
				new FindBookCopyDomain(BARCODE).withRepositories(bookCopies).find().blocks().get(0));
	}

	@Test
	@DisplayName("bloquea el ejemplar apartado para otra persona igual que lo rechaza el registro del préstamo")
	void blocksCopyReservedForSomeoneElseLikeLending() {
		final FakeRepositories.BookCopies bookCopies = bookCopies(BookCopyEntity.STATUS_RESERVED, true);
		final FakeRepositories.Reservations reservations = heldFor(UUIDHelper.generateNew());
		final UUID borrower = UUIDHelper.generateNew();

		final String lending = assertThrows(BookHiveException.class,
				lending(bookCopies, borrower).withReservations(reservations)::validate).getUserMessageKey();

		assertEquals(MessageKey.BOOK_COPY_RESERVED, lending);
		assertEquals(List.of(lending), new FindBookCopyDomain(BARCODE, borrower)
				.withRepositories(bookCopies, reservations).find().blocks());
	}

	@Test
	@DisplayName("el ejemplar apartado no tiene bloqueos para quien lo reservó, igual que el registro del préstamo lo acepta")
	void copyHeldForBorrowerPassesLikeLending() {
		final FakeRepositories.BookCopies bookCopies = bookCopies(BookCopyEntity.STATUS_RESERVED, true);
		final UUID borrower = UUIDHelper.generateNew();
		final FakeRepositories.Reservations reservations = heldFor(borrower);

		final FoundBookCopyDomain found = new FindBookCopyDomain(BARCODE, borrower)
				.withRepositories(bookCopies, reservations).find();

		assertTrue(found.available());
		assertTrue(found.blocks().isEmpty());
		assertDoesNotThrow(lending(bookCopies, borrower).withReservations(reservations)::validate);
	}

	@Test
	@DisplayName("bloquea por consulta en sala igual que lo rechaza el registro del préstamo")
	void blocksReferenceOnlyLikeLending() {
		final FakeRepositories.BookCopies bookCopies = bookCopies(BookCopyEntity.STATUS_RESERVED, false);
		final UUID borrower = UUIDHelper.generateNew();
		final FakeRepositories.Reservations reservations = heldFor(borrower);

		final String lending = assertThrows(BookHiveException.class,
				lending(bookCopies, borrower).withReservations(reservations)::validate).getUserMessageKey();

		assertEquals(MessageKey.BOOK_COPY_REFERENCE_ONLY, lending);
		assertEquals(List.of(lending), new FindBookCopyDomain(BARCODE, borrower)
				.withRepositories(bookCopies, reservations).find().blocks());
	}

	@Test
	@DisplayName("sin código pide que se indique")
	void requiresBarcode() {
		final FindBookCopyDomain search = search("  ", BookCopyEntity.STATUS_AVAILABLE, true);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::validate);

		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
	}

	@Test
	@DisplayName("con código la validación pasa")
	void acceptsBarcode() {
		assertDoesNotThrow(search(BARCODE, BookCopyEntity.STATUS_AVAILABLE, true)::validate);
	}

	@Test
	@DisplayName("si ningún ejemplar tiene ese código avisa que no existe, como el registro del préstamo")
	void rejectsUnknownBarcode() {
		final FindBookCopyDomain search = search("UCO-9999", BookCopyEntity.STATUS_AVAILABLE, true);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.BOOK_COPY_NOT_FOUND, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
	}

	@Test
	@DisplayName("un código más largo de lo que admite la base no se consulta: ningún ejemplar puede tenerlo")
	void rejectsBarcodeLongerThanColumn() {
		final FindBookCopyDomain search = search(
				"U".repeat(FindBookCopyDomain.MAX_BARCODE_LENGTH + 1), BookCopyEntity.STATUS_AVAILABLE, true);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.BOOK_COPY_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("sin repositorio no puede buscar")
	void requiresRepository() {
		final FindBookCopyDomain search = new FindBookCopyDomain(BARCODE);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

	private static FindBookCopyDomain search(final String barcode, final String status, final boolean lendable) {
		return new FindBookCopyDomain(barcode).withRepositories(bookCopies(status, lendable));
	}

	private static FakeRepositories.BookCopies bookCopies(final String status, final boolean lendable) {
		final BookCopyEntity bookCopy = FakeRepositories.bookCopy(BOOK_COPY, status, lendable);
		bookCopy.setBook(BOOK);
		bookCopy.setBarcode(BARCODE);
		return new FakeRepositories.BookCopies().with(bookCopy);
	}

	private static String rejectionOfLending(final FakeRepositories.BookCopies bookCopies) {
		final LendBookDomain loan = lending(bookCopies, UUIDHelper.generateNew());

		return assertThrows(BookHiveException.class, loan::validate).getUserMessageKey();
	}

	private static LendBookDomain lending(final FakeRepositories.BookCopies bookCopies, final UUID user) {
		final FakeRepositories.Parameters parameters = new FakeRepositories.Parameters().with(15, 3);
		return new LendBookDomain(
				UUIDHelper.getDefaultValue(),
				BOOK_COPY,
				user,
				UUIDHelper.generateNew(),
				UUIDHelper.generateNew(),
				CurrentInstitution.today(CurrentInstitution.parameters(parameters)),
				DateHelper.getDefaultValue())
				.withRepositories(
						new FakeRepositories.Users().with(FakeRepositories.user(user, "Ana Gómez", "ana.gomez@uco.edu.co")),
						bookCopies,
						new FakeRepositories.Fines(),
						new FakeRepositories.Loans(),
						parameters);
	}

	private static FakeRepositories.Reservations heldFor(final UUID holder) {
		final ReservationEntity held = FakeRepositories.reservation(
				BOOK, holder, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		held.setBookCopy(BOOK_COPY);
		return new FakeRepositories.Reservations().with(held);
	}

}
