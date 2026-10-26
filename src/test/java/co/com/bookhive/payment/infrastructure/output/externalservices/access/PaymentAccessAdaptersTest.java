package co.com.bookhive.payment.infrastructure.output.externalservices.access;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.access.services.input.dto.response.ListActiveSchemasResponseDTO;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class PaymentAccessAdaptersTest {

	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final UUID PERSON = UUID.randomUUID();

	@AfterEach
	void clearTenant() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("la sesión da la institución, la persona y el esquema en el que se está trabajando")
	void readsSession() {
		final AccessSessionAdapter session =
				new AccessSessionAdapter(() -> new CurrentSessionResponseDTO(INSTITUTION, PERSON));
		TenantContext.set("uco");

		assertEquals(INSTITUTION, session.getInstitution());
		assertEquals(PERSON, session.getPerson());
		assertEquals("uco", session.getSchema());

		TenantContext.clear();
		assertEquals("", session.getSchema());
	}

	@Test
	@DisplayName("el directorio da los esquemas de las instituciones activas")
	void listsActiveSchemas() {
		final AccessInstitutionDirectoryAdapter directory =
				new AccessInstitutionDirectoryAdapter(() -> new ListActiveSchemasResponseDTO(List.of("uco")));

		assertEquals(List.of("uco"), directory.activeSchemas());
	}

}
