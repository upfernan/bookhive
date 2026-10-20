package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;

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
	@DisplayName("si dos préstamos del mismo ejemplar llegan al tiempo el segundo responde 409")
	void simultaneousLoanIsConflict() {
		final ResponseEntity<ErrorResponseDTO> response = handler.handle(new DataIntegrityViolationException(
				"No se pudo guardar.",
				new ConstraintViolationException("Duplicado.", new SQLException("Duplicado.", "23505"),
						"uq_loan_book_copy_active")));

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("Texto de " + MessageKey.BOOK_COPY_NOT_AVAILABLE, messageOf(response));
	}

	@Test
	@DisplayName("el choque también se reconoce cuando solo llega el error del driver")
	void simultaneousLoanFromDriverIsConflict() {
		final ResponseEntity<ErrorResponseDTO> response = handler.handle(new TransactionSystemException(
				"No se pudo confirmar.",
				new SQLException("duplicate key value violates unique constraint \"uq_loan_book_copy_active\"",
						"23505")));

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	}

	@Test
	@DisplayName("otra restricción de la base de datos sigue siendo un error inesperado")
	void otherConstraintIsServerError() {
		final ResponseEntity<ErrorResponseDTO> response = handler.handle(new DataIntegrityViolationException(
				"No se pudo guardar.",
				new ConstraintViolationException("Otra.", new SQLException("Otra.", "23505"), "uq_book_copy_barcode")));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@Test
	@DisplayName("un error del driver que no es de duplicado sigue siendo inesperado")
	void otherDriverErrorIsServerError() {
		final ResponseEntity<ErrorResponseDTO> response = handler.handle(new IllegalStateException(
				"Falló.", new SQLException("uq_loan_book_copy_active", "08006")));

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
