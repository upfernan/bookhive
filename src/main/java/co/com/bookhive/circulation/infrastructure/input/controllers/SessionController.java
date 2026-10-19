package co.com.bookhive.circulation.infrastructure.input.controllers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.circulation.infrastructure.input.security.SessionCookieTokenResolver;
import co.com.bookhive.circulation.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.OpenSessionResponseDTO;
import co.com.bookhive.circulation.services.input.ports.CloseSessionInputPort;
import co.com.bookhive.circulation.services.input.ports.OpenSessionInputPort;
import co.com.bookhive.crosscutting.helper.TextHelper;

@RestController
@RequestMapping(SessionController.PATH)
public class SessionController {

	public static final String PATH = "/api/v1/sesiones";

	private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

	private final OpenSessionInputPort openSessionInputPort;
	private final CloseSessionInputPort closeSessionInputPort;
	private final Duration sessionLength;

	public SessionController(
			final OpenSessionInputPort openSessionInputPort,
			final CloseSessionInputPort closeSessionInputPort,
			@Value("${bookhive.session.minutes:20}") final long sessionMinutes) {

		super();
		this.openSessionInputPort = openSessionInputPort;
		this.closeSessionInputPort = closeSessionInputPort;
		this.sessionLength = Duration.ofMinutes(sessionMinutes);
	}

	@PostMapping
	public ResponseEntity<Void> open(@RequestBody final OpenSessionRequestDTO request) {

		logger.info("Inicia la apertura de una sesión.");

		final OpenSessionResponseDTO session = openSessionInputPort.execute(request);

		logger.info("Sesión abierta.");

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, cookie(session.getSessionToken(), sessionLength))
				.build();
	}

	@DeleteMapping
	public ResponseEntity<Void> close(
			@CookieValue(name = SessionCookieTokenResolver.SESSION_COOKIE, required = false) final String sessionToken) {

		closeSessionInputPort.execute(TextHelper.getDefaultValue(sessionToken));

		logger.info("Sesión cerrada.");

		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, cookie(TextHelper.getDefaultValue(), Duration.ZERO))
				.build();
	}

	private static String cookie(final String value, final Duration maxAge) {

		return ResponseCookie.from(SessionCookieTokenResolver.SESSION_COOKIE, value)
				.httpOnly(true)
				.secure(true)
				.sameSite("Strict")
				.path("/")
				.maxAge(maxAge)
				.build()
				.toString();
	}

}
