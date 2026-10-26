package co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.catalog.services.output.ports.BookCopyStatusPort;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopyStatusEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.testdata.FakeCatalog;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyDetailsUseCaseImplTest {

	private static final UUID BOOK_COPY = UUID.randomUUID();
	private static final UUID BOOK = UUID.randomUUID();

	private List<String> askedBarcodes;
	private List<UUID> askedUsers;
	private BookCopyStatusPort bookCopyStatus;
	private FakeCatalog.Books books;

	@BeforeEach
	void prepare() {
		askedBarcodes = new ArrayList<>();
		askedUsers = new ArrayList<>();
		bookCopyStatus = (barcode, user) -> {
			askedBarcodes.add(barcode);
			askedUsers.add(user);
			return new BookCopyStatusEntity(BOOK_COPY, BOOK, barcode, false, true,
					List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE));
		};
		books = new FakeCatalog.Books().with(new BookEntity(BOOK, "Clean Architecture", "978-0134494166", 2017,
				"FISICO", BookEntity.STATUS_ACTIVE, "Ingeniería de software", "Pearson",
				List.of("Robert C. Martin", "Kevlin Henney")));
	}

	@Test
	@DisplayName("junta el estado del ejemplar con el título y los autores de su libro")
	void combinesBookCopyStatusWithBook() {
		final BookCopyDetailsDomain details = useCase().execute(new FindBookCopyDetailsDomain("UCO-0001"));

		assertEquals(List.of("UCO-0001"), askedBarcodes);
		assertEquals(BOOK_COPY, details.id());
		assertEquals("UCO-0001", details.barcode());
		assertEquals("Clean Architecture", details.title());
		assertEquals("Robert C. Martin, Kevlin Henney", details.authors());
		assertEquals("Sede Centro", details.branch());
		assertFalse(details.available());
		assertTrue(details.lendable());
		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE), details.blocks());
	}

	@Test
	@DisplayName("le pasa al módulo de préstamos el usuario que se lo va a llevar, para saber si el ejemplar es suyo")
	void passesTheBorrower() {
		final UUID borrower = UUID.randomUUID();

		useCase().execute(new FindBookCopyDetailsDomain("UCO-0001", borrower));

		assertEquals(List.of(borrower), askedUsers);
	}

	@Test
	@DisplayName("si el libro no aparece en el catálogo entrega el ejemplar sin título en vez de fallar")
	void toleratesMissingBook() {
		books = new FakeCatalog.Books();

		final BookCopyDetailsDomain details = useCase().execute(new FindBookCopyDetailsDomain("UCO-0001"));

		assertEquals(BOOK_COPY, details.id());
		assertEquals("", details.title());
		assertEquals("", details.authors());
	}

	@Test
	@DisplayName("sin código no le pregunta nada al módulo de préstamos")
	void requiresBarcodeBeforeAsking() {
		final FindBookCopyDetailsUseCaseImpl useCase = useCase();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, exception.getUserMessageKey());
		assertTrue(askedBarcodes.isEmpty());
	}

	@Test
	@DisplayName("deja pasar el rechazo del módulo de préstamos cuando el ejemplar no existe")
	void letsNotFoundPass() {
		bookCopyStatus = (barcode, user) -> {
			throw BookHiveException.createConflict(MessageKey.BOOK_COPY_NOT_FOUND, "No existe.");
		};
		final FindBookCopyDetailsUseCaseImpl useCase = useCase();
		final FindBookCopyDetailsDomain search = new FindBookCopyDetailsDomain("UCO-9999");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(search));

		assertEquals(MessageKey.BOOK_COPY_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no se deja crear sin alguna de sus dependencias")
	void requiresEveryDependency() {
		final BookHiveException withoutStatus = assertThrows(BookHiveException.class,
				() -> new FindBookCopyDetailsUseCaseImpl(null, books, branch -> ""));
		final BookHiveException withoutBooks = assertThrows(BookHiveException.class,
				() -> new FindBookCopyDetailsUseCaseImpl(bookCopyStatus, null, branch -> ""));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, withoutStatus.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, withoutBooks.getKind());
	}

	private FindBookCopyDetailsUseCaseImpl useCase() {
		return new FindBookCopyDetailsUseCaseImpl(bookCopyStatus, books, branch -> "Sede Centro");
	}

}
