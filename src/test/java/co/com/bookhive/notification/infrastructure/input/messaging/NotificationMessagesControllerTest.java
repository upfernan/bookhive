package co.com.bookhive.notification.infrastructure.input.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.notification.services.input.dto.request.SendNotificationRequestDTO;
import co.com.bookhive.notification.services.input.dto.response.SendNotificationResponseDTO;
import co.com.bookhive.shared.infrastructure.input.messaging.PubSubPushMessage;

class NotificationMessagesControllerTest {

	private static final UUID USER = UUID.fromString("0190a000-0000-7000-8000-0000000000aa");
	private static final UUID BOOK = UUID.fromString("0190a000-0000-7000-8000-0000000000bb");
	private static final UUID LOAN = UUID.fromString("0190a000-0000-7000-8000-0000000000cc");

	private final List<String> received = new ArrayList<>();
	private final List<SendNotificationRequestDTO> requests = new ArrayList<>();

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("atiende el aviso dentro del esquema de su institución y lo confirma a Pub/Sub")
	void handlesNoticeInsideInstitution() {
		final NotificationMessagesController controller = new NotificationMessagesController(request -> {
			received.add(TenantContext.get());
			requests.add(request);
			return new SendNotificationResponseDTO("ENVIADO");
		});

		final var response = controller.receive(push("uco", "{\"kind\":\"PRESTAMO_REGISTRADO\",\"user\":\"" + USER
				+ "\",\"book\":\"" + BOOK + "\",\"loan\":\"" + LOAN + "\",\"dueDate\":\"2026-11-06\"}"));

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(List.of("uco"), received);
		assertEquals(new SendNotificationRequestDTO("PRESTAMO_REGISTRADO", USER, BOOK, LOAN, LocalDate.of(2026, 11, 6)),
				requests.get(0));
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("descarta sin reintentar los avisos sin institución válida o con datos dañados")
	void discardsInvalidNotices() {
		final NotificationMessagesController controller = new NotificationMessagesController(request -> {
			requests.add(request);
			throw BookHiveException.create(MessageKey.NOTIFICATION_DATA_INVALID, "Faltan datos.");
		});

		assertEquals(HttpStatus.NO_CONTENT, controller.receive(push("UCO;", "{}")).getStatusCode());
		assertTrue(requests.isEmpty());
		assertEquals(HttpStatus.NO_CONTENT, controller.receive(push("uco", "esto no es json")).getStatusCode());
		assertEquals(HttpStatus.NO_CONTENT, controller.receive(push("uco",
				"{\"user\":\"no-es-uuid\",\"dueDate\":\"ayer\"}")).getStatusCode());
		assertEquals(new SendNotificationRequestDTO("", null, null, null, null), requests.get(0));
		assertEquals(new SendNotificationRequestDTO("", null, null, null, null), requests.get(1));
	}

	@Test
	@DisplayName("si el correo falla responde error para que Pub/Sub lo vuelva a intentar")
	void letsTransientFailuresRetry() {
		final NotificationMessagesController controller = new NotificationMessagesController(request -> {
			throw BookHiveException.createUnexpected(MessageKey.EMAIL_UNAVAILABLE, "Brevo caído.");
		});
		final PubSubPushMessage push = push("uco", "{}");

		assertThrows(BookHiveException.class, () -> controller.receive(push));
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	private static PubSubPushMessage push(final String institution, final String data) {
		return new PubSubPushMessage(new PubSubPushMessage.Message(
				Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8)),
				Map.of("institucion", institution, "tipo", "PRESTAMO_REGISTRADO"), "99"), "s");
	}

}
