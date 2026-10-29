package co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyUseCaseImplTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();
	private static final UUID HOLDER = UUIDHelper.generateNew();

	@Test
	@DisplayName("busca el ejemplar con el repositorio que recibió y entrega su situación")
	void findsBookCopyWithItsRepository() {
		final BookCopyEntity bookCopy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_LENT, true);
		bookCopy.setBarcode("UCO-0001");
		final FindBookCopyUseCaseImpl useCase = new FindBookCopyUseCaseImpl(
				new FakeRepositories.BookCopies().with(bookCopy), new FakeRepositories.Reservations());

		final FoundBookCopyDomain found = useCase.execute(new FindBookCopyDomain("UCO-0001"));

		assertEquals(BOOK_COPY, found.id());
		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE), found.blocks());
	}

	@Test
	@DisplayName("un ejemplar apartado queda libre para quien lo reservó y bloqueado para cualquier otro")
	void reservedCopyIsOnlyForItsHolder() {
		final FindBookCopyUseCaseImpl useCase = useCaseWithReservedCopy();

		final FoundBookCopyDomain forHolder = useCase.execute(new FindBookCopyDomain("UCO-0001", HOLDER));
		final FoundBookCopyDomain forOther = useCase.execute(new FindBookCopyDomain("UCO-0001", UUID.randomUUID()));
		final FoundBookCopyDomain withoutUser = useCase.execute(new FindBookCopyDomain("UCO-0001"));

		assertTrue(forHolder.available());
		assertTrue(forHolder.blocks().isEmpty());
		assertFalse(forOther.available());
		assertEquals(List.of(MessageKey.BOOK_COPY_RESERVED), forOther.blocks());
		assertEquals(List.of(MessageKey.BOOK_COPY_RESERVED), withoutUser.blocks());
	}

	@Test
	@DisplayName("si no llega la búsqueda pide el código")
	void requiresBarcodeWhenSearchIsMissing() {
		final FindBookCopyUseCaseImpl useCase =
				new FindBookCopyUseCaseImpl(new FakeRepositories.BookCopies(), new FakeRepositories.Reservations());

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no se deja crear sin el repositorio de ejemplares ni sin el de reservas")
	void requiresRepositories() {
		final FakeRepositories.BookCopies bookCopies = new FakeRepositories.BookCopies();
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations();

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> new FindBookCopyUseCaseImpl(null, reservations));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertThrows(BookHiveException.class, () -> new FindBookCopyUseCaseImpl(bookCopies, null));
	}

	private static FindBookCopyUseCaseImpl useCaseWithReservedCopy() {
		final BookCopyEntity bookCopy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_RESERVED, true);
		bookCopy.setBarcode("UCO-0001");
		final ReservationEntity held = FakeRepositories.reservation(
				UUID.randomUUID(), HOLDER, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		held.setBookCopy(BOOK_COPY);

		return new FindBookCopyUseCaseImpl(
				new FakeRepositories.BookCopies().with(bookCopy),
				new FakeRepositories.Reservations().with(held));
	}

}
