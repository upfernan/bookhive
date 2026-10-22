package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.response.SyncCatalogResponseDTO;

public interface SyncCatalogInputPort {

	SyncCatalogResponseDTO execute();

}
