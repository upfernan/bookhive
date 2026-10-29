package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import co.com.bookhive.circulation.services.input.dto.request.SearchReadersRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.FoundReaderResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.OpenLoanResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReadyReservationResponseDTO;
import co.com.bookhive.circulation.services.input.ports.SearchReadersInputPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;
import co.com.bookhive.shared.services.input.dto.response.ResolveMessageResponseDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

class UserControllerTest {

	private static final UUID USER = UUIDHelper.generateNew();

	private static final Map<String, String> TEXTS = Map.of(
			MessageKey.USER_WITH_PENDING_FINES, "Tiene multas pendientes.",
			MessageKey.USER_AT_LOAN_LIMIT, "Llegó al máximo de préstamos.");

	private static final ResolveMessageInputPort MESSAGES =
			request -> new ResolveMessageResponseDTO(TEXTS.get(request.messageKey()));

	private static final SearchReadersInputPort NO_READERS = request -> List.of();

	@Test
	@DisplayName("responde 200 con el usuario, sus reservas listas, sus préstamos abiertos y cada bloqueo cambiado por su texto")
	void respondsWithUserAndBlockTexts() {
		final ReadyReservationResponseDTO ready = new ReadyReservationResponseDTO(
				UUIDHelper.generateNew(), UUIDHelper.generateNew(), UUIDHelper.generateNew(), "BH-0001",
				LocalDate.of(2026, 10, 29));
		final OpenLoanResponseDTO open = new OpenLoanResponseDTO(
				UUIDHelper.generateNew(), UUIDHelper.generateNew(), "BH-0007", UUIDHelper.generateNew(),
				LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 16), 2, new BigDecimal("3000"));
		final UserController controller = new UserController(
				request -> new FindUserResponseDTO(USER, "Ana Gómez", request.email(), 3, 3, 1,
						List.of(MessageKey.USER_WITH_PENDING_FINES, MessageKey.USER_AT_LOAN_LIMIT), List.of(ready),
						List.of(open)),
				NO_READERS,
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
		assertEquals(List.of(open), body.openLoans());
	}

	@Test
	@DisplayName("sin bloqueos responde la lista vacía")
	void respondsWithoutBlocks() {
		final UserController controller = new UserController(
				request -> new FindUserResponseDTO(USER, "Ana Gómez", request.email(), 0, 3, 0, List.of(), null, null),
				NO_READERS,
				MESSAGES);

		final FindUserResponseDTO body = controller.find("ana.gomez@uco.edu.co").getBody();

		assertNotNull(body);
		assertEquals(List.of(), body.blocks());
		assertEquals(List.of(), body.readyReservations());
		assertEquals(List.of(), body.openLoans());
	}

	@Test
	@DisplayName("si el usuario no existe deja pasar el error al manejador de errores")
	void letsNotFoundReachTheHandler() {
		final UserController controller = new UserController(request -> {
			throw BookHiveException.createConflict(MessageKey.USER_NOT_FOUND, "No existe.");
		}, NO_READERS, MESSAGES);

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> controller.find("nadie@uco.edu.co"));

		assertEquals(MessageKey.USER_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("la búsqueda de lectores responde 200 con los encontrados y pasa el texto tal cual")
	void searchRespondsWithReaders() {
		final List<SearchReadersRequestDTO> received = new ArrayList<>();
		final FoundReaderResponseDTO ana = new FoundReaderResponseDTO(USER, "Ana Gómez", "ana.gomez@uco.edu.co");
		final UserController controller = new UserController(request -> null, request -> {
			received.add(request);
			return List.of(ana);
		}, MESSAGES);

		final ResponseEntity<List<FoundReaderResponseDTO>> response = controller.search("ana");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(List.of(ana), response.getBody());
		assertEquals(List.of(new SearchReadersRequestDTO("ana")), received);
	}

	@Test
	@DisplayName("si el texto no sirve deja pasar el error al manejador de errores")
	void searchLetsInvalidTextReachTheHandler() {
		final UserController controller = new UserController(request -> null, request -> {
			throw BookHiveException.create(MessageKey.USER_SEARCH_TEXT_INVALID, "Muy corto.");
		}, MESSAGES);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> controller.search("an"));

		assertEquals(MessageKey.USER_SEARCH_TEXT_INVALID, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("buscar lectores y ver a uno exigen el permiso de prestar")
	void requiresLendPermission() throws NoSuchMethodException {
		final RequiredPermission search = UserController.class.getMethod("search", String.class)
				.getAnnotation(RequiredPermission.class);
		final RequiredPermission find = UserController.class.getMethod("find", String.class)
				.getAnnotation(RequiredPermission.class);

		assertEquals(PermissionCode.LEND_BOOK, search.value());
		assertEquals(PermissionCode.LEND_BOOK, find.value());
	}

}
