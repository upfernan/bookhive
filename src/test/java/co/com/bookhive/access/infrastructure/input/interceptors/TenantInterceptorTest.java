package co.com.bookhive.access.infrastructure.input.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantContext;
import co.com.bookhive.access.services.output.ports.SessionContextPort;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class TenantInterceptorTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	private final List<UUID> askedInstitutions = new ArrayList<>();

	private final TenantInterceptor interceptor = new TenantInterceptor(
			new SessionContextPort() {

				@Override
				public UUID getInstitution() {
					return INSTITUTION;
				}

				@Override
				public UUID getLibrarian() {
					return UUIDHelper.generateNew();
				}

			},
			institution -> {
				askedInstitutions.add(institution);
				return "uco";
			});

	@AfterEach
	void clean() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("antes de atender la petición elige el esquema de la institución en sesión")
	void selectsInstitutionSchema() {
		assertTrue(interceptor.preHandle(null, null, new Object()));

		assertEquals("uco", TenantContext.get());
		assertEquals(List.of(INSTITUTION), askedInstitutions);
	}

	@Test
	@DisplayName("al terminar la petición vuelve al esquema sin institución")
	void clearsSchemaAfterRequest() {
		interceptor.preHandle(null, null, new Object());

		interceptor.afterCompletion(null, null, new Object(), null);

		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

}
