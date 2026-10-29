package co.com.bookhive.catalog.infrastructure.output.externalservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.services.input.dto.response.ListActiveSchemasResponseDTO;
import co.com.bookhive.catalog.infrastructure.output.externalservices.access.AccessInstitutionDirectoryAdapter;
import co.com.bookhive.catalog.infrastructure.output.externalservices.circulation.CirculationBookCopiesAdapter;
import co.com.bookhive.catalog.infrastructure.output.externalservices.tenancy.TenantContextScopeAdapter;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopiesEntity;
import co.com.bookhive.circulation.services.input.dto.request.CountBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.BookCopiesDTO;
import co.com.bookhive.circulation.services.input.dto.response.CountBookCopiesResponseDTO;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class CatalogExternalServicesTest {

	@AfterEach
	void clearTenant() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("pide al módulo de préstamos cuántos ejemplares hay de cada libro")
	void asksCirculationForCopies() {
		final UUID book = UUID.randomUUID();
		final List<CountBookCopiesRequestDTO> requests = new ArrayList<>();
		final CirculationBookCopiesAdapter adapter = new CirculationBookCopiesAdapter(request -> {
			requests.add(request);
			return new CountBookCopiesResponseDTO(List.of(new BookCopiesDTO(book, 1, 2)));
		});

		final List<BookCopiesEntity> copies = adapter.countByBooks(List.of(book));

		assertEquals(List.of(book), requests.get(0).books());
		assertEquals(List.of(new BookCopiesEntity(book, 1, 2)), copies);
	}

	@Test
	@DisplayName("pide al módulo de acceso los esquemas de las instituciones activas")
	void asksAccessForSchemas() {
		final AccessInstitutionDirectoryAdapter adapter =
				new AccessInstitutionDirectoryAdapter(() -> new ListActiveSchemasResponseDTO(List.of("uco", "unal")));

		assertEquals(List.of("uco", "unal"), adapter.activeSchemas());
	}

	@Test
	@DisplayName("ejecuta la tarea dentro del esquema pedido y luego lo deja como estaba")
	void runsWithinSchema() {
		final TenantContextScopeAdapter scope = new TenantContextScopeAdapter();
		final List<String> seen = new ArrayList<>();

		scope.within("uco", () -> seen.add(TenantContext.get()));
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());

		TenantContext.set("unal");
		scope.within("uco", () -> seen.add(TenantContext.get()));
		assertEquals("unal", TenantContext.get());

		assertEquals(List.of("uco", "uco"), seen);
	}

	@Test
	@DisplayName("aunque la tarea falle deja el esquema como estaba")
	void restoresSchemaOnFailure() {
		final TenantContextScopeAdapter scope = new TenantContextScopeAdapter();
		final Runnable failing = () -> {
			throw new IllegalStateException("falló");
		};

		assertThrows(IllegalStateException.class, () -> scope.within("uco", failing));

		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

}
