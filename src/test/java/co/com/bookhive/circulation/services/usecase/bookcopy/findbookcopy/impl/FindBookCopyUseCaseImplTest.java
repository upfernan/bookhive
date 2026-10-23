package co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyUseCaseImplTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();

	@Test
	@DisplayName("busca el ejemplar con el repositorio que recibió y entrega su situación")
	void findsBookCopyWithItsRepository() {
		final BookCopyEntity bookCopy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_LENT, true);
		bookCopy.setBarcode("UCO-0001");
		final FindBookCopyUseCaseImpl useCase =
				new FindBookCopyUseCaseImpl(new FakeRepositories.BookCopies().with(bookCopy));

		final FoundBookCopyDomain found = useCase.execute(new FindBookCopyDomain("UCO-0001"));

		assertEquals(BOOK_COPY, found.id());
		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE), found.blocks());
	}

	@Test
	@DisplayName("si no llega la búsqueda pide el código")
	void requiresBarcodeWhenSearchIsMissing() {
		final FindBookCopyUseCaseImpl useCase = new FindBookCopyUseCaseImpl(new FakeRepositories.BookCopies());

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no se deja crear sin el repositorio de ejemplares")
	void requiresRepository() {
		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> new FindBookCopyUseCaseImpl(null));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
