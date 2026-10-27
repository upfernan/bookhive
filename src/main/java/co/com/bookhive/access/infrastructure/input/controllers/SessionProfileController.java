package co.com.bookhive.access.infrastructure.input.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.access.services.input.dto.response.SessionProfileResponseDTO;
import co.com.bookhive.access.services.input.ports.DescribeSessionInputPort;

@RestController
@RequestMapping(SessionProfileController.PATH)
public class SessionProfileController {

	public static final String PATH = "/api/v1/sesiones/actual";

	private final DescribeSessionInputPort describeSession;

	public SessionProfileController(final DescribeSessionInputPort describeSession) {
		super();
		this.describeSession = describeSession;
	}

	@GetMapping
	public ResponseEntity<SessionProfileResponseDTO> current() {
		return ResponseEntity.ok(describeSession.execute());
	}

}
