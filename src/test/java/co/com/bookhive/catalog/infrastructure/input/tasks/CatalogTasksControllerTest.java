package co.com.bookhive.catalog.infrastructure.input.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;

class CatalogTasksControllerTest {

	@Test
	@DisplayName("cuando todos los catálogos llegan al buscador responde 200 con la lista")
	void respondsOkWhenEverySchemaIsSynced() {
		final CatalogTasksController controller = new CatalogTasksController(
				() -> new SyncAllCatalogsResponseDTO(List.of("uco", "unal"), List.of()));

		final ResponseEntity<SyncAllCatalogsResponseDTO> response = controller.syncAllCatalogs();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new SyncAllCatalogsResponseDTO(List.of("uco", "unal"), List.of()), response.getBody());
	}

	@Test
	@DisplayName("si algún catálogo falla responde 503 para que Cloud Scheduler lo marque y reintente")
	void respondsUnavailableWhenSomeSchemaFails() {
		final CatalogTasksController controller = new CatalogTasksController(
				() -> new SyncAllCatalogsResponseDTO(List.of("uco"), List.of("unal: Typesense no respondió")));

		final ResponseEntity<SyncAllCatalogsResponseDTO> response = controller.syncAllCatalogs();

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
		assertEquals(List.of("unal: Typesense no respondió"), response.getBody().failed());
	}

}
