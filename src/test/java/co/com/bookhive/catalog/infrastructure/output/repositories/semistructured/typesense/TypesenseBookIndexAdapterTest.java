package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;
import co.com.bookhive.catalog.testdata.FakeTypesenseServer;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class TypesenseBookIndexAdapterTest {

	private static final String ADMIN_KEY = "clave-admin-de-prueba";
	private static final String SEARCH = "/collections/books/documents/search";
	private static final String IMPORT = "/collections/books/documents/import";
	private static final String DOCUMENTS = "/collections/books/documents";
	private static final UUID BOOK = UUID.fromString("0190a000-0000-7000-8000-0000000000b1");

	private static final String SEARCH_RESULT = """
			{"found": 3, "page": 2,
			 "hits": [
			   {"document": {"id": "uco_0190a000-0000-7000-8000-0000000000b1", "institution": "uco",
							 "book": "0190a000-0000-7000-8000-0000000000b1", "title": "Clean Code",
							 "authors": ["Robert C. Martin"], "category": "Software", "publisher": "Pearson",
							 "format": "FISICO", "year": 2008, "isbn": "978-0132350884"}},
			   {"document": {"id": "uco_roto", "book": "no-es-un-uuid", "title": "Roto"}}
			 ],
			 "facet_counts": [
			   {"field_name": "authors", "counts": [{"value": "Robert C. Martin", "count": 2}]},
			   {"field_name": "category", "counts": [{"value": "Software", "count": 3}]},
			   {"field_name": "publisher", "counts": [{"value": "Pearson", "count": 1}]}
			 ]}
			""";

	private FakeTypesenseServer server;
	private TypesenseBookIndexAdapter adapter;

	@BeforeEach
	void startServer() throws IOException {
		server = new FakeTypesenseServer();
		adapter = new TypesenseBookIndexAdapter(new TypesenseClient(server.url(), ADMIN_KEY));
		TenantContext.set("uco");
	}

	@AfterEach
	void stopServer() {
		TenantContext.clear();
		server.close();
	}

	@Test
	@DisplayName("la primera vez crea la colección y la clave de búsqueda, y busca con la clave restringida a la institución")
	void preparesTypesenseAndSearchesWithScopedKey() {
		server.answer("POST", "/collections", 201, "{}")
				.answer("POST", "/keys", 201, "{}")
				.answer("GET", SEARCH, 200, SEARCH_RESULT);

		final BookSearchPageEntity page = adapter.search(
				new BookSearchCriteria("clean", "Robert C. Martin", "Software", "Pear`son", 2, 20));

		final String searchKey = ScopedSearchKey.derive(ADMIN_KEY, TypesenseBookIndexAdapter.SEARCH_KEY_LABEL);
		final FakeTypesenseServer.Request collection = server.requests("POST", "/collections").get(0);
		assertTrue(collection.body().contains("\"name\":\"title\",\"type\":\"string\",\"sort\":true"));
		assertTrue(collection.body().contains("\"name\":\"authors\",\"type\":\"string[]\",\"facet\":true"));
		final FakeTypesenseServer.Request key = server.requests("POST", "/keys").get(0);
		assertEquals(ADMIN_KEY, key.key());
		assertTrue(key.body().contains("\"actions\":[\"documents:search\"]"));
		assertTrue(key.body().contains("\"collections\":[\"books\"]"));
		assertTrue(key.body().contains("\"value\":\"" + searchKey + "\""));

		final FakeTypesenseServer.Request search = server.requests("GET", SEARCH).get(0);
		assertEquals(ScopedSearchKey.forInstitution(searchKey, "uco"), search.key());
		assertEquals("clean", search.parameters().get("q"));
		assertEquals("title,authors,category,publisher", search.parameters().get("query_by"));
		assertEquals("authors:=`Robert C. Martin` && category:=`Software` && publisher:=`Pearson`",
				search.parameters().get("filter_by"));
		assertEquals("2", search.parameters().get("page"));
		assertEquals("20", search.parameters().get("per_page"));

		assertEquals(3, page.found());
		assertEquals(1, page.books().size());
		final BookEntity book = page.books().get(0);
		assertEquals(BOOK, book.id());
		assertEquals("Clean Code", book.title());
		assertEquals(List.of("Robert C. Martin"), book.authors());
		assertEquals(2008, book.year());
		assertEquals("978-0132350884", book.isbn());
		assertEquals("Robert C. Martin", page.authors().get(0).value());
		assertEquals(3, page.categories().get(0).count());
		assertEquals("Pearson", page.publishers().get(0).value());
	}

	@Test
	@DisplayName("si la colección y la clave ya existen las reutiliza y solo las revisa una vez")
	void reusesExistingCollectionAndKey() {
		server.answer("GET", "/collections/books", 200, "{\"name\":\"books\"}")
				.answer("POST", "/keys", 409, "{\"message\":\"API key generation conflict.\"}")
				.handle("GET", SEARCH, request -> new FakeTypesenseServer.Answer(200, "{\"found\": 0, \"hits\": []}"));

		adapter.search(BookSearchCriteria.countAll());
		final BookSearchPageEntity page = adapter.search(new BookSearchCriteria("", "", "", "", 1, 20));

		assertEquals(0, page.found());
		assertEquals(1, server.requests("GET", "/collections/books").size());
		assertEquals(1, server.requests("POST", "/keys").size());
		assertTrue(server.requests("POST", "/collections").isEmpty());
		final List<FakeTypesenseServer.Request> searches = server.requests("GET", SEARCH);
		assertEquals("*", searches.get(0).parameters().get("q"));
		assertEquals("1", searches.get(0).parameters().get("per_page"));
		assertFalse(searches.get(1).parameters().containsKey("filter_by"));
	}

	@Test
	@DisplayName("guarda los libros por lotes con el identificador de la institución en cada documento")
	void savesBooksInBatches() {
		prepared();
		server.handle("POST", IMPORT, request -> new FakeTypesenseServer.Answer(200,
				request.body().lines().map(line -> "{\"success\":true}").reduce((a, b) -> a + "\n" + b).orElse("")));
		final List<BookEntity> books = new ArrayList<>();
		books.add(new BookEntity(BOOK, "Clean Code", "", 0, "FISICO", BookEntity.STATUS_ACTIVE, "Software", "Pearson",
				List.of("Robert C. Martin")));
		for (int number = 1; number <= 100; number++) {
			books.add(new BookEntity(UUID.randomUUID(), "Libro " + number, "978-" + number, 2000 + number % 20,
					"DIGITAL", BookEntity.STATUS_ACTIVE, "Software", "Pearson", List.of()));
		}

		adapter.save(books);

		final List<FakeTypesenseServer.Request> imports = server.requests("POST", IMPORT);
		assertEquals(2, imports.size());
		assertEquals("upsert", imports.get(0).parameters().get("action"));
		assertEquals(ADMIN_KEY, imports.get(0).key());
		assertEquals(100, imports.get(0).body().lines().count());
		assertEquals(1, imports.get(1).body().lines().count());
		final String first = imports.get(0).body().lines().findFirst().orElse("");
		assertTrue(first.contains("\"id\":\"uco_" + BOOK + "\""));
		assertTrue(first.contains("\"institution\":\"uco\""));
		assertTrue(first.contains("\"authors\":[\"Robert C. Martin\"]"));
		assertFalse(first.contains("\"year\""));
		assertFalse(first.contains("\"isbn\""));
		assertTrue(imports.get(1).body().contains("\"isbn\":\"978-100\""));
	}

	@Test
	@DisplayName("envía los libros en UTF-8 para que las tildes y eñes lleguen completas")
	void sendsBooksInUtf8() {
		prepared();
		server.answer("POST", IMPORT, 200, "{\"success\":true}");

		adapter.save(List.of(new BookEntity(BOOK, "Código General del Proceso", "", 0, "FISICO",
				BookEntity.STATUS_ACTIVE, "Derecho", "Temis", List.of("Hernán Fabio López Blanco"))));

		final FakeTypesenseServer.Request request = server.requests("POST", IMPORT).get(0);
		assertTrue(request.contentType().toLowerCase().contains("charset=utf-8"));
		assertTrue(request.body().contains("\"title\":\"Código General del Proceso\""));
		assertTrue(request.body().contains("Hernán Fabio López Blanco"));
	}

	@Test
	@DisplayName("si Typesense rechaza un libro lo reporta como buscador no disponible")
	void reportsRejectedBook() {
		prepared();
		server.answer("POST", IMPORT, 200, "{\"success\":false,\"error\":\"Field `title` must be a string.\"}");
		final List<BookEntity> books = List.of(new BookEntity(BOOK, "x", "", 0, "", "", "", "", List.of()));

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.save(books));

		assertEquals(MessageKey.SEARCH_UNAVAILABLE, exception.getUserMessageKey());
		assertTrue(exception.getTechnicalMessage().contains("Field `title` must be a string."));
	}

	@Test
	@DisplayName("retira los libros de la institución filtrando por institución y por libro")
	void deletesBooksOfInstitution() {
		prepared();
		server.answer("DELETE", DOCUMENTS, 200, "{\"num_deleted\": 2}");
		final UUID other = UUID.randomUUID();

		adapter.delete(List.of(BOOK, other));

		final FakeTypesenseServer.Request delete = server.requests("DELETE", DOCUMENTS).get(0);
		assertEquals("institution:=uco && book:=[`" + BOOK + "`,`" + other + "`]", delete.parameters().get("filter_by"));
		assertEquals(ADMIN_KEY, delete.key());
	}

	@Test
	@DisplayName("sin libros que guardar o retirar no llama a Typesense")
	void skipsEmptyLists() {
		adapter.save(List.of());
		adapter.save(null);
		adapter.delete(List.of());
		adapter.delete(null);

		assertTrue(server.requests().isEmpty());
	}

	@Test
	@DisplayName("sin dirección o clave configuradas responde que el buscador no está disponible")
	void failsWhenNotConfigured() {
		final TypesenseBookIndexAdapter withoutKey = new TypesenseBookIndexAdapter(new TypesenseClient(server.url(), ""));
		final BookSearchCriteria criteria = BookSearchCriteria.countAll();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> withoutKey.search(criteria));

		assertEquals(MessageKey.SEARCH_UNAVAILABLE, exception.getUserMessageKey());
		assertTrue(server.requests().isEmpty());
	}

	@Test
	@DisplayName("nunca busca sin saber de qué institución es la solicitud")
	void requiresInstitution() {
		TenantContext.clear();
		final BookSearchCriteria criteria = BookSearchCriteria.countAll();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.search(criteria));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertTrue(server.requests().isEmpty());
	}

	@Test
	@DisplayName("si Typesense responde con error o algo que no es JSON responde que el buscador no está disponible")
	void reportsBadResponses() {
		prepared();
		server.answer("GET", SEARCH, 503, "{\"message\":\"Not Ready or Lagging\"}")
				.answer("GET", SEARCH, 200, "<html>proxy</html>");
		final BookSearchCriteria criteria = BookSearchCriteria.countAll();

		final BookHiveException unavailable = assertThrows(BookHiveException.class, () -> adapter.search(criteria));
		final BookHiveException notJson = assertThrows(BookHiveException.class, () -> adapter.search(criteria));

		assertEquals(MessageKey.SEARCH_UNAVAILABLE, unavailable.getUserMessageKey());
		assertTrue(unavailable.getTechnicalMessage().contains("503"));
		assertEquals(MessageKey.SEARCH_UNAVAILABLE, notJson.getUserMessageKey());
	}

	@Test
	@DisplayName("si no puede revisar o crear la colección no sigue")
	void stopsWhenCollectionFails() {
		server.answer("GET", "/collections/books", 500, "x".repeat(300))
				.answer("GET", "/collections/books", 404, "{}")
				.answer("POST", "/collections", 409, "{}")
				.answer("POST", "/keys", 500, "{}");
		final BookSearchCriteria criteria = BookSearchCriteria.countAll();

		final BookHiveException collection = assertThrows(BookHiveException.class, () -> adapter.search(criteria));
		final BookHiveException key = assertThrows(BookHiveException.class, () -> adapter.search(criteria));

		assertTrue(collection.getTechnicalMessage().endsWith("..."));
		assertTrue(key.getTechnicalMessage().contains("clave de búsqueda"));
		assertTrue(server.requests("GET", SEARCH).isEmpty());
	}

	private void prepared() {
		server.answer("GET", "/collections/books", 200, "{}").answer("POST", "/keys", 409, "{}");
	}

}
