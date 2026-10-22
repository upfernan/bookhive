package co.com.bookhive.catalog.services.output.ports.repositories.criteria;

public record CatalogChangeCriteria(int limit) {

	public static final int MAX_LIMIT = 500;

	public CatalogChangeCriteria {
		limit = Math.clamp(limit, 1, MAX_LIMIT);
	}

	public static CatalogChangeCriteria pending(final int limit) {
		return new CatalogChangeCriteria(limit);
	}

}
