package co.com.bookhive.circulation.infrastructure.output.externalservices.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class AccessSessionAdapterTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();

	private final AccessSessionAdapter adapter =
			new AccessSessionAdapter(() -> new CurrentSessionResponseDTO(INSTITUTION, LIBRARIAN));

	@Test
	@DisplayName("toma la institución de la sesión que entrega el módulo de acceso")
	void readsInstitution() {
		assertEquals(INSTITUTION, adapter.getInstitution());
	}

	@Test
	@DisplayName("toma el bibliotecario de la sesión que entrega el módulo de acceso")
	void readsLibrarian() {
		assertEquals(LIBRARIAN, adapter.getLibrarian());
	}

}
