package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.catalog.synccatalog.SyncCatalogDomain;
import co.com.bookhive.catalog.services.input.dto.response.SyncCatalogResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SyncCatalogInputPort;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.SyncCatalogUseCase;

public final class SyncCatalogInputAdapter implements SyncCatalogInputPort {

	private final SyncCatalogUseCase useCase;

	public SyncCatalogInputAdapter(final SyncCatalogUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public SyncCatalogResponseDTO execute() {
		final SyncCatalogDomain result = useCase.execute();
		return new SyncCatalogResponseDTO(result.indexed(), result.removed(), result.fullReload());
	}

}
