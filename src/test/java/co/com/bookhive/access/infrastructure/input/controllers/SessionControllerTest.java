package co.com.bookhive.access.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.access.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.access.services.input.dto.response.OpenSessionResponseDTO;

class SessionControllerTest {

	private List<String> closedSessions;
	private SessionController controller;

	@BeforeEach
	void prepare() {
		closedSessions = new ArrayList<>();
		controller = new SessionController(
				request -> new OpenSessionResponseDTO("cookie-de-" + request.getIdToken()),
				closedSessions::add,
				20);
	}

	@Test
	@DisplayName("al abrir la sesión responde 204 y entrega la cookie protegida por 20 minutos")
	void opensSessionWithProtectedCookie() {
		final ResponseEntity<Void> response = controller.open(new OpenSessionRequestDTO("pase"));

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		final String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertNotNull(cookie);
		assertTrue(cookie.startsWith("bookhive_session=cookie-de-pase"));
		assertTrue(cookie.contains("Max-Age=1200"));
		assertTrue(cookie.contains("HttpOnly"));
		assertTrue(cookie.contains("Secure"));
		assertTrue(cookie.contains("SameSite=Strict"));
		assertTrue(cookie.contains("Path=/"));
	}

	@Test
	@DisplayName("al cerrar la sesión la cierra en el proveedor y borra la cookie del navegador")
	void closesSessionAndClearsCookie() {
		final ResponseEntity<Void> response = controller.close("cookie-de-sesion");

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(List.of("cookie-de-sesion"), closedSessions);
		final String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertNotNull(cookie);
		assertTrue(cookie.startsWith("bookhive_session=;"));
		assertTrue(cookie.contains("Max-Age=0"));
	}

	@Test
	@DisplayName("cerrar sin cookie igual responde 204")
	void closesWithoutCookie() {
		final ResponseEntity<Void> response = controller.close(null);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(List.of(""), closedSessions);
	}

}
