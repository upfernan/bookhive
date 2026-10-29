package co.com.bookhive.circulation.infrastructure.input.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.SearchReadersRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.FoundReaderResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserInputPort;
import co.com.bookhive.circulation.services.input.ports.SearchReadersInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;
import co.com.bookhive.shared.services.input.dto.request.ResolveMessageRequestDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private final FindUserInputPort findUserInputPort;
	private final SearchReadersInputPort searchReadersInputPort;
	private final ResolveMessageInputPort resolveMessageInputPort;

	public UserController(
			final FindUserInputPort findUserInputPort,
			final SearchReadersInputPort searchReadersInputPort,
			final ResolveMessageInputPort resolveMessageInputPort) {

		super();
		this.findUserInputPort = findUserInputPort;
		this.searchReadersInputPort = searchReadersInputPort;
		this.resolveMessageInputPort = resolveMessageInputPort;
	}

	@RequiredPermission(PermissionCode.LEND_BOOK)
	@Transactional(readOnly = true)
	@GetMapping("/busqueda")
	public ResponseEntity<List<FoundReaderResponseDTO>> search(
			@RequestParam(name = "texto", required = false) final String text) {

		logger.info("Inicia la búsqueda de lectores para atenderlos en el mostrador.");

		final List<FoundReaderResponseDTO> found = searchReadersInputPort.execute(new SearchReadersRequestDTO(text));

		logger.info("Búsqueda de lectores atendida con {} resultado(s).", found.size());

		return ResponseEntity.ok(found);
	}

	@RequiredPermission(PermissionCode.LEND_BOOK)
	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<FindUserResponseDTO> find(
			@RequestParam(name = "correo", required = false) final String email) {

		logger.info("Inicia la búsqueda de un usuario para atenderlo en préstamos.");

		final FindUserResponseDTO found = findUserInputPort.execute(new FindUserRequestDTO(email));

		logger.info("Usuario encontrado con {} bloqueo(s) para prestar.", found.blocks().size());

		return ResponseEntity.ok(found.withBlocks(textsOf(found.blocks())));
	}

	private List<String> textsOf(final List<String> messageKeys) {
		return messageKeys.stream()
				.map(messageKey -> resolveMessageInputPort.execute(new ResolveMessageRequestDTO(messageKey)).text())
				.toList();
	}

}
