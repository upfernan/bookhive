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
import co.com.bookhive.circulation.services.input.dto.request.ReturnBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReturnBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ReturnBookInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/devoluciones")
public class ReturnController {

	private static final Logger logger = LoggerFactory.getLogger(ReturnController.class);

	private final ReturnBookInputPort returnBookInputPort;

	public ReturnController(final ReturnBookInputPort returnBookInputPort) {
		super();
		this.returnBookInputPort = returnBookInputPort;
	}

	@RequiredPermission(PermissionCode.RETURN_BOOK)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping
	public ResponseEntity<ReturnBookResponseDTO> returnBook(@RequestBody final ReturnBookRequestDTO request) {
		logger.info("Inicia el registro de una devolución.");
		final ReturnBookResponseDTO response = returnBookInputPort.execute(request);
		final int overdueDays = response.overdueDays();
		logger.info("Devolución registrada con {} días de retraso.", overdueDays);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

}
