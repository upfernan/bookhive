package co.com.bookhive.access.infrastructure.input.controllers;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.access.services.input.dto.request.RegisterPersonRequestDTO;
import co.com.bookhive.access.services.input.dto.response.RegisterPersonResponseDTO;
import co.com.bookhive.access.services.input.ports.RegisterPersonInputPort;

@RestController
@RequestMapping(RegistrationController.PATH)
public class RegistrationController {

	public static final String PATH = "/api/v1/registro";

	private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

	private final RegisterPersonInputPort registerPerson;
	private final Duration sessionLength;

	public RegistrationController(
			final RegisterPersonInputPort registerPerson,
			@Value("${bookhive.session.minutes:20}") final long sessionMinutes) {
		super();
		this.registerPerson = registerPerson;
		this.sessionLength = Duration.ofMinutes(sessionMinutes);
	}

	@PostMapping
	public ResponseEntity<Void> register(@RequestBody(required = false) final RegisterPersonRequestDTO request) {
		final RegisterPersonResponseDTO registered = registerPerson.execute(request);
		logger.info("Se registró una persona nueva y quedó con la sesión abierta.");
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, SessionController.cookie(registered.sessionToken(), sessionLength))
				.build();
	}

}
