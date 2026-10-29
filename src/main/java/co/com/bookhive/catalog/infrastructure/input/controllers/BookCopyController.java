package co.com.bookhive.catalog.infrastructure.input.controllers;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.catalog.services.input.ports.FindBookCopyDetailsInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;
import co.com.bookhive.shared.services.input.dto.request.ResolveMessageRequestDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

@RestController
@RequestMapping("/api/v1/ejemplares")
public class BookCopyController {

	private static final Logger logger = LoggerFactory.getLogger(BookCopyController.class);

	private final FindBookCopyDetailsInputPort findBookCopyDetailsInputPort;
	private final ResolveMessageInputPort resolveMessageInputPort;

	public BookCopyController(
			final FindBookCopyDetailsInputPort findBookCopyDetailsInputPort,
			final ResolveMessageInputPort resolveMessageInputPort) {

		super();
		this.findBookCopyDetailsInputPort = findBookCopyDetailsInputPort;
		this.resolveMessageInputPort = resolveMessageInputPort;
	}

	@RequiredPermission(PermissionCode.LEND_BOOK)
	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<FindBookCopyDetailsResponseDTO> find(
			@RequestParam(name = "codigo", required = false) final String barcode,
			@RequestParam(name = "usuario", required = false) final UUID user) {

		logger.info("Inicia la búsqueda de un ejemplar para prestarlo.");

		final FindBookCopyDetailsResponseDTO found =
				findBookCopyDetailsInputPort.execute(new FindBookCopyDetailsRequestDTO(barcode, user));

		logger.info("Ejemplar encontrado con {} bloqueo(s) para prestar.", found.blocks().size());

		return ResponseEntity.ok(found.withBlocks(textsOf(found.blocks())));
	}

	private List<String> textsOf(final List<String> messageKeys) {
		return messageKeys.stream()
				.map(messageKey -> resolveMessageInputPort.execute(new ResolveMessageRequestDTO(messageKey)).text())
				.toList();
	}

}
