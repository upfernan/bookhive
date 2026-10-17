package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.circulation.infrastructure.input.controllers.dto.ErrorResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class CirculationExceptionHandlerTest {

	private final CirculationExceptionHandler handler =
			new CirculationExceptionHandler(messageKey -> "Texto de " + messageKey);

	@Test
	@DisplayName("un dato faltante responde 400 con el mensaje del catálogo")
	void missingDataIsBadRequest() {
		final ResponseEntity<ErrorResponseDTO> response =
				handler.handle(BookHiveException.create(MessageKey.LOAN_USER_REQUIRED, "Falta el usuario."));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.LOAN_USER_REQUIRED, response.getBody().getMessage());
	}

	@Test
	@DisplayName("un rechazo del negocio responde 409")
	void businessRejectionIsConflict() {
		final ResponseEntity<ErrorResponseDTO> response =
				handler.handle(BookHiveException.createConflict(MessageKey.USER_WITH_PENDING_FINES, "Tiene multas."));

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.USER_WITH_PENDING_FINES, response.getBody().getMessage());
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
		assertEquals("Texto de " + MessageKey.REQUEST_UNHANDLED, response.getBody().getMessage());
	}

}
