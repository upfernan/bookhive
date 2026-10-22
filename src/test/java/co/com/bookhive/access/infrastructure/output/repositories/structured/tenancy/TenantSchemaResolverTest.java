package co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy;

import co.com.bookhive.crosscutting.tenancy.TenantContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TenantSchemaResolverTest {

	private final TenantSchemaResolver resolver = new TenantSchemaResolver();

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("le dice a Hibernate el esquema de la institución en curso")
	void resolvesCurrentSchema() {
		TenantContext.set("institucion_uco");

		assertEquals("institucion_uco", resolver.resolveCurrentTenantIdentifier());
		assertFalse(resolver.validateExistingCurrentSessions());
	}

}
