package co.com.bookhive.catalog.domain.catalog.syncallcatalogs;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record SyncAllCatalogsDomain(List<String> synced, List<String> failed) {

	public SyncAllCatalogsDomain {
		synced = List.copyOf(ObjectHelper.getDefaultValue(synced, List.of()));
		failed = List.copyOf(ObjectHelper.getDefaultValue(failed, List.of()));
	}

}
