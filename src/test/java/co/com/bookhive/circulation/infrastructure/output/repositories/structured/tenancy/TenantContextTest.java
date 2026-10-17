package co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TenantContextTest {

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("sin institución usa el esquema público")
	void usesPublicSchemaByDefault() {
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("guarda el esquema de la institución sin espacios")
	void keepsInstitutionSchema() {
		TenantContext.set("  institucion_uco  ");

		assertEquals("institucion_uco", TenantContext.get());
	}

	@Test
	@DisplayName("un esquema vacío vuelve al público")
	void blankSchemaIsPublic() {
		TenantContext.set(null);

		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("al limpiar vuelve al esquema público")
	void clearReturnsToPublicSchema() {
		TenantContext.set("institucion_uco");

		TenantContext.clear();

		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

}
