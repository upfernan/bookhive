package co.com.bookhive.circulation.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/prestamos")
public class LoanController {

	private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

	private final LendBookInputPort lendBookInputPort;

	public LoanController(final LendBookInputPort lendBookInputPort) {
		super();
		this.lendBookInputPort = lendBookInputPort;
	}

	@RequiredPermission(PermissionCode.LEND_BOOK)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping
	public ResponseEntity<LendBookResponseDTO> lend(
			@RequestBody final LendBookRequestDTO request) {

		logger.info("Inicia el registro de un préstamo.");

		final LendBookResponseDTO response = lendBookInputPort.execute(request);

		logger.info("Préstamo registrado.");

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

}
