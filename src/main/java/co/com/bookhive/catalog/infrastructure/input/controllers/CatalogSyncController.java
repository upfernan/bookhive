package co.com.bookhive.catalog.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.catalog.services.input.dto.response.SyncCatalogResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SyncCatalogInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/catalogo/sincronizacion")
public class CatalogSyncController {

	private static final Logger logger = LoggerFactory.getLogger(CatalogSyncController.class);

	private final SyncCatalogInputPort syncCatalogInputPort;

	public CatalogSyncController(final SyncCatalogInputPort syncCatalogInputPort) {
		super();
		this.syncCatalogInputPort = syncCatalogInputPort;
	}

	@RequiredPermission(PermissionCode.SYNC_CATALOG)
	@PostMapping
	public ResponseEntity<SyncCatalogResponseDTO> sync() {

		logger.info("Inicia la actualización del buscador del catálogo.");

		final SyncCatalogResponseDTO response = syncCatalogInputPort.execute();

		logger.info("Buscador actualizado: {} libros enviados y {} retirados.", response.indexed(), response.removed());

		return ResponseEntity.ok(response);
	}

}
