package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;

class BookCopyCriteriaTest {

	@Test
	@DisplayName("los ejemplares en servicio son los de esos libros que no están dados de baja")
	void inServiceExcludesWithdrawn() {
		final UUID book = UUID.randomUUID();
		final BookCopyCriteria criteria = BookCopyCriteria.inService(List.of(book));

		assertEquals(List.of(book), criteria.getBooks());
		assertTrue(criteria.filtersByBooks());
		assertTrue(criteria.excludesStatus());
		assertEquals(BookCopyEntity.STATUS_WITHDRAWN, criteria.getExcludedStatus());
		assertFalse(BookCopyCriteria.inService(null).filtersByBooks());
	}

}
