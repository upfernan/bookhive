package co.com.bookhive.catalog.services.usecase.book.findbooktitle.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.domain.book.findbooktitle.FindBookTitleDomain;
import co.com.bookhive.catalog.services.input.adapters.FindBookTitleInputAdapter;
import co.com.bookhive.catalog.services.input.dto.request.FindBookTitleRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.BookTitleResponseDTO;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.testdata.FakeCatalog;

class FindBookTitleUseCaseImplTest {

	private final BookEntity book = FakeCatalog.book("Clean Code", BookEntity.STATUS_ACTIVE);
	private final FakeCatalog.Books books = new FakeCatalog.Books().with(book);

	@Test
	@DisplayName("devuelve el título del libro pedido")
	void findsTitle() {
		assertEquals(new FindBookTitleDomain(book.id(), "Clean Code"),
				new FindBookTitleUseCaseImpl(books).execute(new FindBookTitleDomain(book.id(), "")));
	}

	@Test
	@DisplayName("sin libro o si no existe devuelve el título vacío sin fallar")
	void returnsEmptyTitle() {
		final FindBookTitleUseCaseImpl useCase = new FindBookTitleUseCaseImpl(books);
		final UUID missing = UUID.randomUUID();

		assertEquals("", useCase.execute(null).title());
		assertEquals(new FindBookTitleDomain(missing, ""), useCase.execute(new FindBookTitleDomain(missing, "")));
		assertEquals(1, books.searches().size());
	}

	@Test
	@DisplayName("el puerto de entrada devuelve el libro y su título")
	void inputAdapterReturnsTitle() {
		final FindBookTitleInputAdapter adapter = new FindBookTitleInputAdapter(new FindBookTitleUseCaseImpl(books));

		assertEquals(new BookTitleResponseDTO(book.id(), "Clean Code"), adapter.execute(new FindBookTitleRequestDTO(book.id())));
		assertEquals(new BookTitleResponseDTO(null, ""), adapter.execute(null));
	}

}
