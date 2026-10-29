package co.com.bookhive.catalog.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SearchBooksInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/libros")
public class BookController {

	private static final Logger logger = LoggerFactory.getLogger(BookController.class);

	private final SearchBooksInputPort searchBooksInputPort;

	public BookController(final SearchBooksInputPort searchBooksInputPort) {
		super();
		this.searchBooksInputPort = searchBooksInputPort;
	}

	@RequiredPermission(PermissionCode.SEARCH_CATALOG)
	@GetMapping
	public ResponseEntity<SearchBooksResponseDTO> search(
			@RequestParam(name = "texto", required = false) final String text,
			@RequestParam(name = "autor", required = false) final String author,
			@RequestParam(name = "categoria", required = false) final String category,
			@RequestParam(name = "editorial", required = false) final String publisher,
			@RequestParam(name = "pagina", defaultValue = "1") final int page) {

		logger.info("Inicia una búsqueda en el catálogo.");

		final SearchBooksResponseDTO response = searchBooksInputPort.execute(
				new SearchBooksRequestDTO(text, author, category, publisher, page));

		logger.info("Búsqueda atendida con {} resultados.", response.found());

		return ResponseEntity.ok(response);
	}

}
