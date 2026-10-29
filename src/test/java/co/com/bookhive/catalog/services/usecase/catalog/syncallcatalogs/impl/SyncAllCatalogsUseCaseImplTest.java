package co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.domain.catalog.syncallcatalogs.SyncAllCatalogsDomain;
import co.com.bookhive.catalog.domain.catalog.synccatalog.SyncCatalogDomain;
import co.com.bookhive.catalog.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.catalog.services.output.ports.TenantScopePort;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.SyncCatalogUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SyncAllCatalogsUseCaseImplTest {

	private final List<String> visited = new ArrayList<>();
	private String current = "";

	private final TenantScopePort tenantScope = (schema, action) -> {
		current = schema;
		try {
			action.run();
		} finally {
			current = "";
		}
	};

	@Test
	@DisplayName("sincroniza cada institución activa dentro de su propio esquema y sigue aunque una falle")
	void syncsEveryInstitutionAndKeepsGoing() {
		final InstitutionDirectoryPort directory = () -> List.of("uco", "unal", "udea");
		final SyncCatalogUseCase sync = () -> {
			visited.add(current);
			if ("unal".equals(current)) {
				throw BookHiveException.createUnexpected(MessageKey.SEARCH_UNAVAILABLE, "Typesense no respondió.");
			}
			if ("udea".equals(current)) {
				throw new IllegalStateException("se cayó");
			}
			return new SyncCatalogDomain(1, 0, false);
		};

		final SyncAllCatalogsDomain result = new SyncAllCatalogsUseCaseImpl(directory, tenantScope, sync).execute();

		assertEquals(List.of("uco", "unal", "udea"), visited);
		assertEquals(List.of("uco"), result.synced());
		assertEquals(List.of("unal: Typesense no respondió.", "udea: IllegalStateException se cayó"), result.failed());
	}

	@Test
	@DisplayName("no se puede crear sin sus puertos")
	void requiresDependencies() {
		final InstitutionDirectoryPort directory = List::of;
		final SyncCatalogUseCase sync = () -> new SyncCatalogDomain(0, 0, false);

		assertThrows(BookHiveException.class, () -> new SyncAllCatalogsUseCaseImpl(null, tenantScope, sync));
		assertThrows(BookHiveException.class, () -> new SyncAllCatalogsUseCaseImpl(directory, null, sync));
		assertThrows(BookHiveException.class, () -> new SyncAllCatalogsUseCaseImpl(directory, tenantScope, null));
	}

}
