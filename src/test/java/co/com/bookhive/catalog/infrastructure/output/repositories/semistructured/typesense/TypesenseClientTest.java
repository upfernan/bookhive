package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import co.com.bookhive.catalog.testdata.FakeTypesenseServer;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class TypesenseClientTest {

	@Test
	@DisplayName("envía la clave en el encabezado y codifica bien los parámetros con espacios y símbolos")
	void sendsKeyAndEncodesParameters() throws IOException {
		try (FakeTypesenseServer server = new FakeTypesenseServer()) {
			server.answer("GET", "/collections/books/documents/search", 200, "{\"found\":0}");
			final TypesenseClient client = new TypesenseClient(server.url(), "admin");
			final Map<String, String> parameters = new LinkedHashMap<>();
			parameters.put("q", "cien años & más+");
			parameters.put("filter_by", "category:=`Ciencia ficción` && authors:=`O'Neil`");

			final TypesenseResponse response = client.get("/collections/books/documents/search", parameters, "llave");

			assertTrue(response.isSuccess());
			assertEquals("{\"found\":0}", response.body());
			final FakeTypesenseServer.Request request = server.requests().get(0);
			assertEquals("llave", request.key());
			assertEquals("cien años & más+", request.parameters().get("q"));
			assertEquals("category:=`Ciencia ficción` && authors:=`O'Neil`", request.parameters().get("filter_by"));
		}
	}

	@Test
	@DisplayName("envía el cuerpo con su tipo y también sabe borrar")
	void postsBodyAndDeletes() throws IOException {
		try (FakeTypesenseServer server = new FakeTypesenseServer()) {
			server.answer("POST", "/collections", 201, "{}");
			server.answer("DELETE", "/collections/books/documents", 200, "{\"num_deleted\":1}");
			final TypesenseClient client = new TypesenseClient(server.url(), "admin");

			final TypesenseResponse created = client.post("/collections", Map.of(), "admin", "{\"name\":\"books\"}",
					MediaType.APPLICATION_JSON);
			final TypesenseResponse deleted = client.delete("/collections/books/documents", Map.of("filter_by", "book:=[`a`]"),
					"admin");

			assertEquals(201, created.status());
			assertEquals(200, deleted.status());
			assertEquals("{\"name\":\"books\"}", server.requests().get(0).body());
			assertTrue(server.requests().get(0).contentType().startsWith("application/json"));
			assertEquals("book:=[`a`]", server.requests().get(1).parameters().get("filter_by"));
		}
	}

	@Test
	@DisplayName("devuelve los errores de Typesense como respuesta sin lanzar")
	void returnsErrorStatus() throws IOException {
		try (FakeTypesenseServer server = new FakeTypesenseServer()) {
			final TypesenseResponse response = new TypesenseClient(server.url(), "admin").get("/nada", Map.of(), "admin");

			assertEquals(404, response.status());
			assertFalse(response.isSuccess());
		}
	}

	@Test
	@DisplayName("si Typesense no contesta responde que el buscador no está disponible")
	void reportsUnreachableServer() {
		final TypesenseClient client = new TypesenseClient("http://127.0.0.1:1", "admin");
		final Map<String, String> none = Map.of();

		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> client.get("/health", none, "admin"));

		assertEquals(MessageKey.SEARCH_UNAVAILABLE, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("solo está configurado con dirección y clave")
	void knowsWhenConfigured() {
		assertTrue(new TypesenseClient("http://10.142.0.2:8108", "admin").isConfigured());
		assertFalse(new TypesenseClient("", "admin").isConfigured());
		assertFalse(new TypesenseClient("http://10.142.0.2:8108", null).isConfigured());
		assertEquals("admin", new TypesenseClient("x", " admin ").adminKey());
		assertTrue(new TypesenseResponse(204, null).isSuccess());
		assertFalse(new TypesenseResponse(199, "").isSuccess());
		assertEquals("", new TypesenseResponse(500, null).body());
	}

}
