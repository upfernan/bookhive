package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.catalog.syncallcatalogs.SyncAllCatalogsDomain;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SyncAllCatalogsInputPort;
import co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.SyncAllCatalogsUseCase;

public final class SyncAllCatalogsInputAdapter implements SyncAllCatalogsInputPort {

	private final SyncAllCatalogsUseCase useCase;

	public SyncAllCatalogsInputAdapter(final SyncAllCatalogsUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public SyncAllCatalogsResponseDTO execute() {
		final SyncAllCatalogsDomain result = useCase.execute();
		return new SyncAllCatalogsResponseDTO(result.synced(), result.failed());
	}

}
