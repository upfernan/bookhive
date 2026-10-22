package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ProvisionalSessionContextTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();

	@Test
	@DisplayName("toma la institución y el bibliotecario de la configuración")
	void readsConfiguredSession() {
		final ProvisionalSessionContext context =
				new ProvisionalSessionContext(" " + INSTITUTION + " ", LIBRARIAN.toString());
		context.warnAboutProvisionalContext();

		assertEquals(INSTITUTION, context.getInstitution());
		assertEquals(LIBRARIAN, context.getLibrarian());
	}

	@Test
	@DisplayName("si falta la configuración avisa que no se pudo resolver la sesión")
	void rejectsMissingConfiguration() {
		final ProvisionalSessionContext context = new ProvisionalSessionContext("", null);

		final BookHiveException exception = assertThrows(BookHiveException.class, context::getInstitution);

		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
		assertThrows(BookHiveException.class, context::getLibrarian);
	}

	@Test
	@DisplayName("si la configuración no tiene forma de identificador avisa que no se pudo resolver la sesión")
	void rejectsMalformedConfiguration() {
		final ProvisionalSessionContext context = new ProvisionalSessionContext("no-es-un-id", "tampoco");

		final BookHiveException exception = assertThrows(BookHiveException.class, context::getInstitution);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

}
