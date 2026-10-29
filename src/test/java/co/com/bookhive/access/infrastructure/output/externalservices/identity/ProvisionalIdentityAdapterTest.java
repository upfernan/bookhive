package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ProvisionalIdentityAdapterTest {

	@Test
	@DisplayName("toma la identidad de la configuración")
	void readsConfiguredIdentity() {
		final ProvisionalIdentityAdapter adapter = new ProvisionalIdentityAdapter(" uid-de-prueba ");
		adapter.warnAboutProvisionalIdentity();

		assertEquals("uid-de-prueba", adapter.getIdentity());
	}

	@Test
	@DisplayName("si falta la configuración avisa que no se pudo resolver la sesión")
	void rejectsMissingConfiguration() {
		final ProvisionalIdentityAdapter adapter = new ProvisionalIdentityAdapter(null);

		final BookHiveException exception = assertThrows(BookHiveException.class, adapter::getIdentity);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

}
