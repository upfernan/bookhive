package co.com.bookhive.notification.domain.notification.sendnotification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SendNotificationDomainTest {

	private static final UUID USER = UUID.randomUUID();
	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID LOAN = UUID.fromString("0190a000-0000-7000-8000-0000000000cc");
	private static final LocalDate DUE = LocalDate.of(2026, 11, 6);

	@Test
	@DisplayName("cada tipo de aviso usa su asunto y su cuerpo del catálogo de mensajes")
	void picksTemplatesByKind() {
		final SendNotificationDomain registered = notice(SendNotificationDomain.KIND_LOAN_REGISTERED);
		final SendNotificationDomain dueSoon = notice(SendNotificationDomain.KIND_LOAN_DUE_SOON);

		assertEquals(MessageKey.EMAIL_LOAN_REGISTERED_SUBJECT, registered.subjectKey());
		assertEquals(MessageKey.EMAIL_LOAN_REGISTERED_BODY, registered.bodyKey());
		assertEquals(MessageKey.EMAIL_LOAN_DUE_SOON_SUBJECT, dueSoon.subjectKey());
		assertEquals(MessageKey.EMAIL_LOAN_DUE_SOON_BODY, dueSoon.bodyKey());
		assertEquals(MessageKey.EMAIL_FINE_GENERATED_SUBJECT, notice(SendNotificationDomain.KIND_FINE_GENERATED).subjectKey());
		assertEquals(MessageKey.EMAIL_FINE_GENERATED_BODY, notice(SendNotificationDomain.KIND_FINE_GENERATED).bodyKey());
		assertEquals("", notice("OTRO").subjectKey());
		assertDoesNotThrow(registered::validate);
	}

	@Test
	@DisplayName("el mismo aviso del mismo préstamo y fecha siempre tiene la misma llave, para no enviarlo dos veces")
	void idempotencyKeyIsStable() {
		assertEquals("PRESTAMO_REGISTRADO:" + LOAN + ":2026-11-06",
				notice(SendNotificationDomain.KIND_LOAN_REGISTERED).idempotencyKey());
	}

	@Test
	@DisplayName("llena el texto con el nombre, el título y la fecha, y pone palabras neutras si faltan")
	void fillsTemplates() {
		final SendNotificationDomain notice = notice(SendNotificationDomain.KIND_LOAN_DUE_SOON);

		assertEquals("Hola Ana, «Clean Code» vence el 06/11/2026",
				notice.fill("Hola {nombre}, «{titulo}» vence el {fecha}", "Ana", "Clean Code"));
		assertEquals("Hola lector, «que tienes prestado» vence el 06/11/2026",
				notice.fill("Hola {nombre}, «{titulo}» vence el {fecha}", "", null));
		assertEquals("", notice.fill(null, "Ana", "x"));
	}

	@Test
	@DisplayName("rechaza avisos de un tipo desconocido o sin usuario, libro, préstamo o fecha")
	void rejectsIncompleteNotices() {
		final List<SendNotificationDomain> invalid = List.of(
				notice("OTRO"),
				new SendNotificationDomain(SendNotificationDomain.KIND_LOAN_DUE_SOON, null, BOOK, LOAN, DUE),
				new SendNotificationDomain(SendNotificationDomain.KIND_LOAN_DUE_SOON, USER, null, LOAN, DUE),
				new SendNotificationDomain(SendNotificationDomain.KIND_LOAN_DUE_SOON, USER, BOOK, null, DUE),
				new SendNotificationDomain(SendNotificationDomain.KIND_LOAN_DUE_SOON, USER, BOOK, LOAN, null));

		for (final SendNotificationDomain notice : invalid) {
			final BookHiveException exception = assertThrows(BookHiveException.class, notice::validate);
			assertEquals(MessageKey.NOTIFICATION_DATA_INVALID, exception.getUserMessageKey());
			assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
		}
		assertEquals("", new NotificationOutcomeDomain(null).status());
	}

	private static SendNotificationDomain notice(final String kind) {
		return new SendNotificationDomain(kind, USER, BOOK, LOAN, DUE);
	}

}
