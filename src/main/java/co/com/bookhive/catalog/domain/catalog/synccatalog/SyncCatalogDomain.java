package co.com.bookhive.catalog.domain.catalog.synccatalog;

public record SyncCatalogDomain(int indexed, int removed, boolean fullReload) {

	public SyncCatalogDomain {
		indexed = Math.max(indexed, 0);
		removed = Math.max(removed, 0);
	}

}
