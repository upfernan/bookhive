package co.com.bookhive.catalog.infrastructure.input.tasks;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SyncAllCatalogsInputPort;

@RestController
@RequestMapping("/api/v1/tareas")
public class CatalogTasksController {

	private static final Logger logger = LoggerFactory.getLogger(CatalogTasksController.class);

	private final SyncAllCatalogsInputPort syncAllCatalogs;

	public CatalogTasksController(final SyncAllCatalogsInputPort syncAllCatalogs) {
		super();
		this.syncAllCatalogs = syncAllCatalogs;
	}

	@PostMapping("/sincronizar-catalogo")
	public ResponseEntity<SyncAllCatalogsResponseDTO> syncAllCatalogs() {
		logger.info("Cloud Scheduler pidió actualizar el buscador del catálogo.");
		final SyncAllCatalogsResponseDTO response = syncAllCatalogs.execute();
		final List<String> synced = response.synced();
		final List<String> failed = response.failed();

		if (!failed.isEmpty()) {
			logger.warn("Catálogos llevados al buscador: {}. No se pudieron llevar: {}.", synced, failed);
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
		}

		logger.info("Catálogos llevados al buscador: {}.", synced);
		return ResponseEntity.ok(response);
	}

}
