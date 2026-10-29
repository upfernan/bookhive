package co.com.bookhive.catalog.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.catalog.services.input.dto.response.SyncCatalogResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

class CatalogControllersTest {

	@Test
	@DisplayName("la búsqueda responde 200 con los libros encontrados y pasa los filtros tal cual")
	void searchRespondsOk() {
		final List<SearchBooksRequestDTO> received = new ArrayList<>();
		final BookController controller = new BookController(request -> {
			received.add(request);
			return new SearchBooksResponseDTO(4, request.page(), List.of(), List.of(), List.of(), List.of());
		});

		final ResponseEntity<SearchBooksResponseDTO> response =
				controller.search("clean", "Martin", "Software", "Pearson", 2);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(4, response.getBody().found());
		assertEquals(List.of(new SearchBooksRequestDTO("clean", "Martin", "Software", "Pearson", 2)), received);
	}

	@Test
	@DisplayName("si el buscador no está disponible deja pasar el error al manejador")
	void searchLetsErrorsThrough() {
		final BookController controller = new BookController(request -> {
			throw BookHiveException.createUnexpected(MessageKey.SEARCH_UNAVAILABLE, "Typesense caído.");
		});

		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> controller.search(null, null, null, null, 1));

		assertEquals(MessageKey.SEARCH_UNAVAILABLE, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("la actualización del buscador responde 200 con el resumen")
	void syncRespondsOk() {
		final CatalogSyncController controller = new CatalogSyncController(() -> new SyncCatalogResponseDTO(5, 1, false));

		final ResponseEntity<SyncCatalogResponseDTO> response = controller.sync();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new SyncCatalogResponseDTO(5, 1, false), response.getBody());
	}

	@Test
	@DisplayName("buscar y actualizar el buscador exigen permisos distintos")
	void requiresPermissions() throws NoSuchMethodException {
		final RequiredPermission search = BookController.class
				.getMethod("search", String.class, String.class, String.class, String.class, int.class)
				.getAnnotation(RequiredPermission.class);
		final RequiredPermission sync = CatalogSyncController.class.getMethod("sync")
				.getAnnotation(RequiredPermission.class);

		assertEquals(PermissionCode.SEARCH_CATALOG, search.value());
		assertEquals(PermissionCode.SYNC_CATALOG, sync.value());
	}

}
