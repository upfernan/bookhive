package co.com.bookhive.catalog.domain.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.domain.catalog.syncallcatalogs.SyncAllCatalogsDomain;
import co.com.bookhive.catalog.domain.catalog.synccatalog.SyncCatalogDomain;

class CatalogSyncDomainTest {

	@Test
	@DisplayName("el resumen de una sincronización nunca trae cantidades negativas")
	void syncSummaryIsNeverNegative() {
		final SyncCatalogDomain summary = new SyncCatalogDomain(-1, -2, true);

		assertEquals(0, summary.indexed());
		assertEquals(0, summary.removed());
		assertTrue(summary.fullReload());
	}

	@Test
	@DisplayName("el resumen de todas las instituciones nunca trae listas nulas")
	void allCatalogsSummaryHasLists() {
		final SyncAllCatalogsDomain summary = new SyncAllCatalogsDomain(null, null);

		assertEquals(List.of(), summary.synced());
		assertEquals(List.of(), summary.failed());
	}

}
