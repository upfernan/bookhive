package co.com.bookhive.catalog.services.output.ports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopiesEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.CatalogChangeCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogChangeEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.FacetCountEntity;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

class CatalogPortEntitiesTest {

	private static final UUID EMPTY = new UUID(0, 0);

	@Test
	@DisplayName("un libro sin datos queda vacío, inactivo y sin autores")
	void bookHasSafeDefaults() {
		final BookEntity book = new BookEntity(null, null, null, -1, null, null, null, null, null);

		assertEquals(EMPTY, book.id());
		assertEquals("", book.title());
		assertEquals(0, book.year());
		assertEquals(List.of(), book.authors());
		assertFalse(book.isActive());
		assertTrue(new BookEntity(null, "", "", 0, "", BookEntity.STATUS_ACTIVE, "", "", List.of()).isActive());
	}

	@Test
	@DisplayName("los resultados, facetas, cambios y conteos nunca quedan nulos ni negativos")
	void searchResultsHaveSafeDefaults() {
		final BookSearchPageEntity page = new BookSearchPageEntity(-1, null, null, null, null);

		assertEquals(0, page.found());
		assertEquals(List.of(), page.books());
		assertEquals(List.of(), page.authors());
		assertEquals(List.of(), page.categories());
		assertEquals(List.of(), page.publishers());
		assertEquals(new FacetCountEntity("", 0), new FacetCountEntity(null, -4));
		assertEquals(new CatalogChangeEntity(EMPTY, EMPTY), new CatalogChangeEntity(null, null));
		assertEquals(EMPTY, new BookCopiesEntity(null, 0, 0).book());
	}

	@Test
	@DisplayName("los criterios limpian sus valores y respetan los límites")
	void criteriaRespectLimits() {
		final BookSearchCriteria search = new BookSearchCriteria(null, null, null, null, 0, -1);
		assertEquals(new BookSearchCriteria("", "", "", "", 1, 0), search);
		assertEquals(search, BookSearchCriteria.countAll());

		final BookCriteria byIds = BookCriteria.byIds(null);
		assertFalse(byIds.filtersByIds());
		assertEquals("", byIds.getStatus());

		final BookCriteria active = BookCriteria.active(null);
		assertEquals(BookEntity.STATUS_ACTIVE, active.getStatus());
		assertEquals(PageCriteria.defaultPage().getSize(), active.getPage().getSize());

		assertEquals(1, CatalogChangeCriteria.pending(0).limit());
		assertEquals(CatalogChangeCriteria.MAX_LIMIT, CatalogChangeCriteria.pending(10_000).limit());
	}

}
