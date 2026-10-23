package co.com.bookhive.notification.infrastructure.output.externalservices.brevo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.notification.services.output.ports.entities.EmailEntity;
import co.com.bookhive.shared.testdata.FakeHttpServer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class BrevoEmailAdapterTest {

	private static final String SEND = "/v3/smtp/email";
	private static final EmailEntity EMAIL =
			new EmailEntity("ana@uco.edu.co", "Ana Pérez", "Quedó registrado tu préstamo", "Hola Ana:\n\nDevuélvelo el 06/11/2026.");

	@Test
	@DisplayName("envía el correo a Brevo con la llave, el remitente, el destinatario y el texto, y devuelve su número")
	void sendsEmail() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("POST", SEND, 201, "{\"messageId\":\"<202610221900.1@smtp-relay>\"}");
			final BrevoEmailAdapter adapter =
					new BrevoEmailAdapter(server.url() + SEND, "llave-de-prueba", "bookhive870@gmail.com", "BookHive");

			final String messageId = adapter.send(EMAIL);

			assertEquals("<202610221900.1@smtp-relay>", messageId);
			final FakeHttpServer.Request request = server.requests().get(0);
			assertEquals("llave-de-prueba", request.header(BrevoEmailAdapter.API_KEY_HEADER));
			assertTrue(request.header("Content-Type").toLowerCase().contains("charset=utf-8"));
			final JsonNode body = JsonMapper.shared().readTree(request.body());
			assertEquals("bookhive870@gmail.com", body.path("sender").path("email").asString());
			assertEquals("BookHive", body.path("sender").path("name").asString());
			assertEquals("ana@uco.edu.co", body.path("to").get(0).path("email").asString());
			assertEquals("Ana Pérez", body.path("to").get(0).path("name").asString());
			assertEquals("Quedó registrado tu préstamo", body.path("subject").asString());
			assertEquals("Hola Ana:\n\nDevuélvelo el 06/11/2026.", body.path("textContent").asString());
		}
	}

	@Test
	@DisplayName("sin nombre del destinatario no manda el campo vacío, y una respuesta sin número no falla")
	void omitsEmptyName() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("POST", SEND, 201, "aceptado");
			final BrevoEmailAdapter adapter = new BrevoEmailAdapter(server.url() + SEND, "llave", "bookhive870@gmail.com", "BookHive");

			assertEquals("", adapter.send(new EmailEntity("ana@uco.edu.co", "", "Asunto", "Texto")));
			assertFalse(server.requests().get(0).body().contains("\"name\":\"\""));
		}
	}

	@Test
	@DisplayName("si Brevo rechaza, no responde o no hay llave, avisa que el correo no está disponible")
	void reportsFailures() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("POST", SEND, 401, "{\"code\":\"unauthorized\",\"message\":\"Key not found\"}");
			final BrevoEmailAdapter rejected = new BrevoEmailAdapter(server.url() + SEND, "mala", "bookhive870@gmail.com", "BookHive");
			final BrevoEmailAdapter unreachable =
					new BrevoEmailAdapter("http://127.0.0.1:1", "llave", "bookhive870@gmail.com", "BookHive");
			final BrevoEmailAdapter withoutKey = new BrevoEmailAdapter(server.url() + SEND, "", "bookhive870@gmail.com", "BookHive");
			final BrevoEmailAdapter withoutSender = new BrevoEmailAdapter(server.url() + SEND, "llave", "", "BookHive");

			final BookHiveException unauthorized = assertThrows(BookHiveException.class, () -> rejected.send(EMAIL));
			assertTrue(unauthorized.getTechnicalMessage().contains("401"));
			assertTrue(unauthorized.getTechnicalMessage().contains("Key not found"));
			for (final BrevoEmailAdapter adapter : new BrevoEmailAdapter[] { unreachable, withoutKey, withoutSender }) {
				final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.send(EMAIL));
				assertEquals(MessageKey.EMAIL_UNAVAILABLE, exception.getUserMessageKey());
			}
			assertEquals(1, server.requests().size());
		}
	}

}
