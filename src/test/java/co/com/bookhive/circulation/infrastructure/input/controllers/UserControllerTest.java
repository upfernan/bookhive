package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReadyReservationResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.input.dto.response.ResolveMessageResponseDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

class UserControllerTest {

	private static final UUID USER = UUIDHelper.generateNew();

	private static final Map<String, String> TEXTS = Map.of(
			MessageKey.USER_WITH_PENDING_FINES, "Tiene multas pendientes.",
			MessageKey.USER_AT_LOAN_LIMIT, "Llegó al máximo de préstamos.");

	private static final ResolveMessageInputPort MESSAGES =
			request -> new ResolveMessageResponseDTO(TEXTS.get(request.messageKey()));

	@Test
	@DisplayName("responde 200 con el usuario, sus reservas listas y cada bloqueo cambiado por su texto")
	void respondsWithUserAndBlockTexts() {
		final ReadyReservationResponseDTO ready = new ReadyReservationResponseDTO(
				UUIDHelper.generateNew(), UUIDHelper.generateNew(), UUIDHelper.generateNew(), "BH-0001",
				LocalDate.of(2026, 10, 29));
		final UserController controller = new UserController(
				request -> new FindUserResponseDTO(USER, "Ana Gómez", request.email(), 3, 3, 1,
						List.of(MessageKey.USER_WITH_PENDING_FINES, MessageKey.USER_AT_LOAN_LIMIT), List.of(ready)),
				MESSAGES);

		final ResponseEntity<FindUserResponseDTO> response = controller.find("ana.gomez@uco.edu.co");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		final FindUserResponseDTO body = response.getBody();
		assertNotNull(body);
		assertEquals(USER, body.id());
		assertEquals("ana.gomez@uco.edu.co", body.email());
		assertEquals(3, body.activeLoans());
		assertEquals(3, body.maxLoans());
		assertEquals(1, body.pendingFines());
		assertEquals(List.of("Tiene multas pendientes.", "Llegó al máximo de préstamos."), body.blocks());
		assertEquals(List.of(ready), body.readyReservations());
	}

	@Test
	@DisplayName("sin bloqueos responde la lista vacía")
	void respondsWithoutBlocks() {
		final UserController controller = new UserController(
				request -> new FindUserResponseDTO(USER, "Ana Gómez", request.email(), 0, 3, 0, List.of(), null),
				MESSAGES);

		final FindUserResponseDTO body = controller.find("ana.gomez@uco.edu.co").getBody();

		assertNotNull(body);
		assertEquals(List.of(), body.blocks());
		assertEquals(List.of(), body.readyReservations());
	}

	@Test
	@DisplayName("si el usuario no existe deja pasar el error al manejador de errores")
	void letsNotFoundReachTheHandler() {
		final UserController controller = new UserController(request -> {
			throw BookHiveException.createConflict(MessageKey.USER_NOT_FOUND, "No existe.");
		}, MESSAGES);

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> controller.find("nadie@uco.edu.co"));

		assertEquals(MessageKey.USER_NOT_FOUND, exception.getUserMessageKey());
	}

}
