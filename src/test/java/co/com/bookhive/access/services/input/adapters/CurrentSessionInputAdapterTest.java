package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.access.services.output.ports.SessionContextPort;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class CurrentSessionInputAdapterTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();

	@Test
	@DisplayName("entrega la institución y el bibliotecario de la sesión en curso")
	void returnsCurrentSession() {
		final CurrentSessionResponseDTO session = adapter(INSTITUTION, LIBRARIAN).execute();

		assertEquals(INSTITUTION, session.institution());
		assertEquals(LIBRARIAN, session.librarian());
	}

	@Test
	@DisplayName("si la sesión no trae datos responde con identificadores por defecto")
	void returnsDefaultsWhenMissing() {
		final CurrentSessionResponseDTO session = adapter(null, null).execute();

		assertEquals(UUIDHelper.getDefaultValue(), session.institution());
		assertEquals(UUIDHelper.getDefaultValue(), session.librarian());
	}

	private static CurrentSessionInputAdapter adapter(final UUID institution, final UUID librarian) {
		return new CurrentSessionInputAdapter(new SessionContextPort() {

			@Override
			public UUID getInstitution() {
				return institution;
			}

			@Override
			public UUID getLibrarian() {
				return librarian;
			}

		});
	}

}
