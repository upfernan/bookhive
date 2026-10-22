package co.com.bookhive.catalog.domain.book.searchbooks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SearchBooksDomainTest {

	private static final String TOO_LONG = "x".repeat(SearchBooksDomain.MAX_TEXT_LENGTH + 1);

	@Test
	@DisplayName("los textos vacíos o nulos quedan como texto vacío y sin espacios sobrantes")
	void cleansTexts() {
		final SearchBooksDomain search = new SearchBooksDomain(null, "  Martin ", null, " ", 1);

		assertEquals("", search.text());
		assertEquals("Martin", search.author());
		assertEquals("", search.category());
		assertEquals("", search.publisher());
	}

	@Test
	@DisplayName("una búsqueda sin filtros en la primera página es válida")
	void acceptsEmptySearch() {
		assertDoesNotThrow(() -> new SearchBooksDomain("", "", "", "", 1).validate());
		assertDoesNotThrow(() -> new SearchBooksDomain("", "", "", "", SearchBooksDomain.MAX_PAGE).validate());
	}

	@Test
	@DisplayName("rechaza cualquier campo de más de 100 caracteres")
	void rejectsLongFields() {
		final List<SearchBooksDomain> searches = List.of(
				new SearchBooksDomain(TOO_LONG, "", "", "", 1),
				new SearchBooksDomain("", TOO_LONG, "", "", 1),
				new SearchBooksDomain("", "", TOO_LONG, "", 1),
				new SearchBooksDomain("", "", "", TOO_LONG, 1));

		for (final SearchBooksDomain search : searches) {
			final BookHiveException exception = assertThrows(BookHiveException.class, search::validate);
			assertEquals(MessageKey.SEARCH_TEXT_TOO_LONG, exception.getUserMessageKey());
			assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
		}
	}

	@Test
	@DisplayName("rechaza páginas fuera del rango de 1 a 100")
	void rejectsPagesOutOfRange() {
		for (final int page : new int[] { 0, -1, SearchBooksDomain.MAX_PAGE + 1 }) {
			final SearchBooksDomain search = new SearchBooksDomain("", "", "", "", page);
			final BookHiveException exception = assertThrows(BookHiveException.class, search::validate);
			assertEquals(MessageKey.SEARCH_PAGE_INVALID, exception.getUserMessageKey());
		}
	}

	@Test
	@DisplayName("un libro encontrado nunca queda con datos nulos ni copias negativas")
	void foundBookHasSafeDefaults() {
		final FoundBookDomain book = new FoundBookDomain(null, null, null, null, null, null, 0, null, -3, -1);

		assertEquals(new UUID(0, 0), book.id());
		assertEquals("", book.title());
		assertEquals(List.of(), book.authors());
		assertEquals(0, book.availableCopies());
		assertEquals(0, book.totalCopies());

		final FoundBookDomain withCopies = book.withCopies(2, 5);
		assertEquals(2, withCopies.availableCopies());
		assertEquals(5, withCopies.totalCopies());
		assertEquals(3, book.withCopies(3, 1).totalCopies());
	}

	@Test
	@DisplayName("el resultado y las facetas nunca quedan nulos")
	void foundBooksHasSafeDefaults() {
		final FoundBooksDomain found = new FoundBooksDomain(-1, 1, null, null, null, null);
		final FacetValueDomain facet = new FacetValueDomain(null, -2);

		assertEquals(0, found.found());
		assertEquals(List.of(), found.books());
		assertEquals(List.of(), found.authors());
		assertEquals(List.of(), found.categories());
		assertEquals(List.of(), found.publishers());
		assertEquals("", facet.value());
		assertEquals(0, facet.count());
	}

}
