package co.com.bookhive.catalog.services.usecase.book.searchbooks.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBookDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.FacetCountEntity;
import co.com.bookhive.catalog.testdata.FakeCatalog;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SearchBooksUseCaseImplTest {

	private final BookEntity cleanCode = FakeCatalog.book("Clean Code", BookEntity.STATUS_ACTIVE);
	private final BookEntity refactoring = FakeCatalog.book("Refactoring", BookEntity.STATUS_ACTIVE);

	@Test
	@DisplayName("busca con los filtros pedidos y agrega cuántos ejemplares hay de cada libro")
	void searchesAndAddsCopies() {
		final FakeCatalog.Index index = new FakeCatalog.Index().answering(new BookSearchPageEntity(
				2,
				List.of(cleanCode, refactoring),
				List.of(new FacetCountEntity("Robert C. Martin", 1)),
				List.of(new FacetCountEntity("Ingeniería de software", 2)),
				List.of(new FacetCountEntity("Pearson", 2))));
		final FakeCatalog.Copies copies = new FakeCatalog.Copies().with(cleanCode.id(), 2, 3);
		final SearchBooksUseCaseImpl useCase = new SearchBooksUseCaseImpl(index, copies);

		final FoundBooksDomain found = useCase.execute(new SearchBooksDomain("clean", "Robert C. Martin", "", "", 2));

		assertEquals(new BookSearchCriteria("clean", "Robert C. Martin", "", "", 2, SearchBooksDomain.PAGE_SIZE),
				index.searches().get(0));
		assertEquals(List.of(List.of(cleanCode.id(), refactoring.id())), copies.requests());
		assertEquals(2, found.found());
		assertEquals(2, found.page());

		final FoundBookDomain first = found.books().get(0);
		assertEquals("Clean Code", first.title());
		assertEquals(List.of("Robert C. Martin"), first.authors());
		assertEquals(2, first.availableCopies());
		assertEquals(3, first.totalCopies());

		final FoundBookDomain second = found.books().get(1);
		assertEquals(0, second.availableCopies());
		assertEquals(0, second.totalCopies());

		assertEquals("Robert C. Martin", found.authors().get(0).value());
		assertEquals(2, found.categories().get(0).count());
		assertEquals("Pearson", found.publishers().get(0).value());
	}

	@Test
	@DisplayName("si no encuentra libros no pregunta por ejemplares")
	void doesNotCountCopiesWithoutResults() {
		final FakeCatalog.Copies copies = new FakeCatalog.Copies();
		final SearchBooksUseCaseImpl useCase = new SearchBooksUseCaseImpl(new FakeCatalog.Index(), copies);

		final FoundBooksDomain found = useCase.execute(null);

		assertEquals(0, found.found());
		assertEquals(1, found.page());
		assertTrue(copies.requests().isEmpty());
	}

	@Test
	@DisplayName("rechaza la búsqueda inválida antes de consultar el buscador")
	void validatesBeforeSearching() {
		final FakeCatalog.Index index = new FakeCatalog.Index();
		final SearchBooksUseCaseImpl useCase = new SearchBooksUseCaseImpl(index, new FakeCatalog.Copies());
		final SearchBooksDomain search = new SearchBooksDomain("", "", "", "", 0);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(search));

		assertEquals(MessageKey.SEARCH_PAGE_INVALID, exception.getUserMessageKey());
		assertTrue(index.searches().isEmpty());
	}

	@Test
	@DisplayName("no se puede crear sin el buscador ni sin el conteo de ejemplares")
	void requiresDependencies() {
		final FakeCatalog.Index index = new FakeCatalog.Index();
		final FakeCatalog.Copies copies = new FakeCatalog.Copies();

		assertThrows(BookHiveException.class, () -> new SearchBooksUseCaseImpl(null, copies));
		assertThrows(BookHiveException.class, () -> new SearchBooksUseCaseImpl(index, null));
	}

}
