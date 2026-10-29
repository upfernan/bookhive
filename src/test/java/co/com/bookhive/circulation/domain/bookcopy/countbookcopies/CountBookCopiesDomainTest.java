package co.com.bookhive.circulation.domain.bookcopy.countbookcopies;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class CountBookCopiesDomainTest {

	@Test
	@DisplayName("descarta libros nulos, vacíos y repetidos")
	void cleansBooks() {
		final UUID book = UUID.randomUUID();

		final CountBookCopiesDomain domain =
				new CountBookCopiesDomain(Arrays.asList(book, null, new UUID(0, 0), book));

		assertEquals(List.of(book), domain.books());
		assertEquals(List.of(), new CountBookCopiesDomain(null).books());
	}

	@Test
	@DisplayName("acepta hasta 100 libros por consulta y rechaza más")
	void limitsBooksPerQuery() {
		final List<UUID> books = new ArrayList<>();
		for (int number = 0; number < CountBookCopiesDomain.MAX_BOOKS; number++) {
			books.add(UUID.randomUUID());
		}
		assertDoesNotThrow(() -> new CountBookCopiesDomain(books).validate());

		books.add(UUID.randomUUID());
		final CountBookCopiesDomain tooMany = new CountBookCopiesDomain(books);
		final BookHiveException exception = assertThrows(BookHiveException.class, tooMany::validate);
		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("el conteo nunca queda negativo ni con menos ejemplares en total que disponibles")
	void countIsConsistent() {
		final BookCopiesCountDomain count = new BookCopiesCountDomain(null, -1, -1);
		assertEquals(new UUID(0, 0), count.book());
		assertEquals(0, count.available());
		assertEquals(0, count.total());
		assertEquals(4, new BookCopiesCountDomain(UUID.randomUUID(), 4, 2).total());
	}

}
