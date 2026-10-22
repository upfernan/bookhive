package co.com.bookhive.catalog.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.BookDetailsRow;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.BookJpaRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

class BookRepositoryAdapterTest {

	private static final UUID BOOK = UUID.randomUUID();

	private final List<String> calls = new ArrayList<>();

	@Test
	@DisplayName("trae los libros pedidos con sus autores separados")
	void findsBooksByIds() {
		final List<BookEntity> books = adapter(row(BOOK, "Ana\u001FBeto\u001F ", 2017))
				.findByCriteria(BookCriteria.byIds(List.of(BOOK)));

		assertEquals(List.of("ids:[" + BOOK + "]"), calls);
		final BookEntity book = books.get(0);
		assertEquals(BOOK, book.id());
		assertEquals("Clean Architecture", book.title());
		assertEquals(List.of("Ana", "Beto"), book.authors());
		assertEquals(2017, book.year());
		assertTrue(book.isActive());
	}

	@Test
	@DisplayName("pide los libros activos por página, con el desplazamiento que corresponde")
	void findsActivePage() {
		final List<BookEntity> books = adapter(row(BOOK, null, null))
				.findByCriteria(BookCriteria.active(PageCriteria.of(2, 50)));

		assertEquals(List.of("estado:ACTIVO tamaño:50 desde:100"), calls);
		assertEquals(List.of(), books.get(0).authors());
		assertEquals(0, books.get(0).year());
	}

	@Test
	@DisplayName("sin criterio, o sin libros ni estado, no consulta la base")
	void skipsQueriesWithoutCriteria() {
		final BookRepositoryAdapter adapter = adapter(row(BOOK, null, null));

		assertTrue(adapter.findByCriteria(null).isEmpty());
		assertTrue(adapter.findByCriteria(BookCriteria.byIds(List.of())).isEmpty());
		assertTrue(calls.isEmpty());
	}

	@Test
	@DisplayName("cuenta solo los libros activos")
	void countsActiveBooks() {
		assertEquals(7, adapter(row(BOOK, null, null)).countActive());
		assertEquals(List.of("contar:ACTIVO"), calls);
	}

	private BookRepositoryAdapter adapter(final BookDetailsRow row) {
		final BookJpaRepository repository = (BookJpaRepository) Proxy.newProxyInstance(
				BookJpaRepository.class.getClassLoader(),
				new Class<?>[] { BookJpaRepository.class },
				(proxy, method, arguments) -> switch (method.getName()) {
					case "findDetailsByIds" -> {
						calls.add("ids:" + List.copyOf((Collection<?>) arguments[0]));
						yield List.of(row);
					}
					case "findDetailsByStatus" -> {
						calls.add("estado:" + arguments[0] + " tamaño:" + arguments[1] + " desde:" + arguments[2]);
						yield List.of(row);
					}
					case "countByStatus" -> {
						calls.add("contar:" + arguments[0]);
						yield 7L;
					}
					default -> throw new UnsupportedOperationException(method.getName());
				});
		return new BookRepositoryAdapter(repository);
	}

	private static BookDetailsRow row(final UUID id, final String authors, final Integer year) {
		return new BookDetailsRow() {

			@Override
			public UUID getId() {
				return id;
			}

			@Override
			public String getTitle() {
				return "Clean Architecture";
			}

			@Override
			public String getIsbn() {
				return "978-0134494166";
			}

			@Override
			public Integer getYear() {
				return year;
			}

			@Override
			public String getFormat() {
				return "FISICO";
			}

			@Override
			public String getStatus() {
				return BookEntity.STATUS_ACTIVE;
			}

			@Override
			public String getCategory() {
				return "Software";
			}

			@Override
			public String getPublisher() {
				return "Pearson";
			}

			@Override
			public String getAuthors() {
				return authors;
			}

		};
	}

}
