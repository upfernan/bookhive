package co.com.bookhive.access.infrastructure.input.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.input.dto.response.ResolveSchemaResponseDTO;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class TenantInterceptorTest {

	private final TenantInterceptor interceptor = new TenantInterceptor(() -> new ResolveSchemaResponseDTO("uco"));

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("antes de atender la petición elige el esquema de la institución en sesión")
	void selectsInstitutionSchema() {
		assertTrue(interceptor.preHandle(null, null, new Object()));

		assertEquals("uco", TenantContext.get());
	}

	@Test
	@DisplayName("al terminar la petición vuelve al esquema sin institución")
	void clearsSchemaAfterRequest() {
		interceptor.preHandle(null, null, new Object());

		interceptor.afterCompletion(null, null, new Object(), null);

		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

}
