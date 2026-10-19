package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LoanControllerTest {

	private static final UUID LOAN = UUIDHelper.generateNew();
	private static final LocalDate DUE_DATE = LocalDate.of(2026, 11, 1);

	@Test
	@DisplayName("registra el préstamo y responde 201 con el número y la fecha de entrega")
	void lendsAndRespondsCreated() {
		final LoanController controller = new LoanController(request -> new LendBookResponseDTO(LOAN, DUE_DATE));

		final ResponseEntity<LendBookResponseDTO> response = controller.lend(request());

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		final LendBookResponseDTO body = response.getBody();
		assertNotNull(body);
		assertEquals(LOAN, body.getLoan());
		assertEquals(DUE_DATE, body.getDueDate());
	}

	@Test
	@DisplayName("si el préstamo se rechaza deja pasar el error al manejador de errores")
	void letsRejectionReachTheHandler() {
		final LoanController controller = new LoanController(request -> {
			throw BookHiveException.createConflict(MessageKey.USER_WITH_PENDING_FINES, "Tiene multas pendientes.");
		});
		final LendBookRequestDTO request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> controller.lend(request));

		assertEquals(MessageKey.USER_WITH_PENDING_FINES, exception.getUserMessageKey());
	}

	private static LendBookRequestDTO request() {
		return new LendBookRequestDTO(UUIDHelper.generateNew(), UUIDHelper.generateNew());
	}

}
