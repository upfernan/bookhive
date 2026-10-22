package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatalogJpaEntitiesTest {

	@Test
	@DisplayName("el libro de la base nunca queda con datos nulos")
	void bookHasSafeDefaults() {
		final BookJpaEntity book = new BookJpaEntity();
		assertEquals(new UUID(0, 0), book.getId());
		assertEquals("", book.getTitle());
		assertEquals("", book.getStatus());

		final UUID id = UUID.randomUUID();
		book.setId(id);
		book.setTitle(" Clean Code ");
		book.setStatus(null);
		assertEquals(id, book.getId());
		assertEquals("Clean Code", book.getTitle());
		assertEquals("", book.getStatus());
	}

	@Test
	@DisplayName("el cambio pendiente nunca queda con datos nulos")
	void changeHasSafeDefaults() {
		final CatalogChangeJpaEntity change = new CatalogChangeJpaEntity();
		final Instant now = Instant.now();
		change.setChangedAt(now);
		change.setBook(null);

		assertEquals(new UUID(0, 0), change.getId());
		assertEquals(new UUID(0, 0), change.getBook());
		assertEquals(now, change.getChangedAt());
	}

}
