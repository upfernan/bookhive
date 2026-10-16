package co.com.bookhive.circulation.infrastructure.input.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;

@RestController
@RequestMapping("/api/v1/prestamos")
public class LoanController {

	private final LendBookInputPort lendBookInputPort;

	public LoanController(final LendBookInputPort lendBookInputPort) {
		super();
		this.lendBookInputPort = lendBookInputPort;
	}

	@PostMapping
	public ResponseEntity<LendBookResponseDTO> lend(
			@RequestBody final LendBookRequestDTO request) {

		final LendBookResponseDTO response = lendBookInputPort.execute(request);

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

}
