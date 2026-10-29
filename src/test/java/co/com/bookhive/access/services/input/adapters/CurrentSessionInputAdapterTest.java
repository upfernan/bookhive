package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class CurrentSessionInputAdapterTest {

	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	@Test
	@DisplayName("entrega la institución y el bibliotecario de la sesión que resolvió el caso de uso")
	void returnsCurrentSession() {
		final CurrentSessionResponseDTO session =
				new CurrentSessionInputAdapter(() -> new ResolveSessionDomain(PERSON, INSTITUTION)).execute();

		assertEquals(INSTITUTION, session.institution());
		assertEquals(PERSON, session.librarian());
	}

	@Test
	@DisplayName("si la sesión no trae datos responde con identificadores por defecto")
	void returnsDefaultsWhenMissing() {
		final CurrentSessionResponseDTO session =
				new CurrentSessionInputAdapter(() -> new ResolveSessionDomain(null, null)).execute();

		assertEquals(UUIDHelper.getDefaultValue(), session.institution());
		assertEquals(UUIDHelper.getDefaultValue(), session.librarian());
	}

}
