package co.com.bookhive.shared.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.infrastructure.input.controllers.dto.ErrorResponseDTO;
import co.com.bookhive.shared.services.input.dto.response.ResolveMessageResponseDTO;

class ApiExceptionHandlerTest {

	private final ApiExceptionHandler handler =
			new ApiExceptionHandler(request -> new ResolveMessageResponseDTO("Texto de " + request.messageKey()));

	@Test
	@DisplayName("un dato faltante responde 400 con el mensaje del catálogo")
	void missingDataIsBadRequest() {
		final ResponseEntity<ErrorResponseDTO> response =
				handler.handle(BookHiveException.create(MessageKey.LOAN_USER_REQUIRED, "Falta el usuario."));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.LOAN_USER_REQUIRED, messageOf(response));
	}

	@Test
	@DisplayName("un rechazo del negocio responde 409")
	void businessRejectionIsConflict() {
		final ResponseEntity<ErrorResponseDTO> response =
				handler.handle(BookHiveException.createConflict(MessageKey.USER_WITH_PENDING_FINES, "Tiene multas."));

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.USER_WITH_PENDING_FINES, messageOf(response));
	}

	@Test
	@DisplayName("una acción sin permiso responde 403 con el mensaje del catálogo")
	void missingPermissionIsForbidden() {
		final ResponseEntity<ErrorResponseDTO> response =
				handler.handle(BookHiveException.createNotAllowed(MessageKey.PERMISSION_DENIED, "Sin permiso."));

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.PERMISSION_DENIED, messageOf(response));
	}

	@Test
	@DisplayName("un error inesperado propio responde 500")
	void unexpectedErrorIsServerError() {
		final ResponseEntity<ErrorResponseDTO> response =
				handler.handle(BookHiveException.createUnexpected(MessageKey.LOAN_UNEXPECTED, "Falló algo."));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@Test
	@DisplayName("cualquier otro error responde 500 con el mensaje genérico")
	void anyOtherErrorIsServerError() {
		final ResponseEntity<ErrorResponseDTO> response = handler.handle(new IllegalStateException("Falló algo."));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.REQUEST_UNHANDLED, messageOf(response));
	}

	private static String messageOf(final ResponseEntity<ErrorResponseDTO> response) {
		final ErrorResponseDTO body = response.getBody();
		assertNotNull(body);
		return body.getMessage();
	}

}
