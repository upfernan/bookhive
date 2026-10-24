package co.com.bookhive.catalog.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.input.dto.response.ResolveMessageResponseDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

class BookCopyControllerTest {

	private static final UUID BOOK_COPY = UUID.randomUUID();

	private static final Map<String, String> TEXTS = Map.of(
			MessageKey.BOOK_COPY_NOT_AVAILABLE, "El ejemplar no está disponible.",
			MessageKey.BOOK_COPY_REFERENCE_ONLY, "Solo se consulta en sala.");

	private static final ResolveMessageInputPort MESSAGES =
			request -> new ResolveMessageResponseDTO(TEXTS.get(request.messageKey()));

	@Test
	@DisplayName("responde 200 con el ejemplar y cambia cada bloqueo por su texto para mostrarlo")
	void respondsWithBookCopyAndBlockTexts() {
		final BookCopyController controller = new BookCopyController(
				request -> new FindBookCopyDetailsResponseDTO(BOOK_COPY, request.barcode(), "Clean Architecture",
						"Robert C. Martin", "", false, false,
						List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE, MessageKey.BOOK_COPY_REFERENCE_ONLY)),
				MESSAGES);

		final ResponseEntity<FindBookCopyDetailsResponseDTO> response = controller.find("UCO-0001", null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		final FindBookCopyDetailsResponseDTO body = response.getBody();
		assertNotNull(body);
		assertEquals(BOOK_COPY, body.id());
		assertEquals("UCO-0001", body.barcode());
		assertEquals("Clean Architecture", body.title());
		assertEquals("Robert C. Martin", body.authors());
		assertEquals("", body.branch());
		assertFalse(body.available());
		assertFalse(body.lendable());
		assertEquals(List.of("El ejemplar no está disponible.", "Solo se consulta en sala."), body.blocks());
	}

	@Test
	@DisplayName("sin bloqueos responde la lista vacía")
	void respondsWithoutBlocks() {
		final BookCopyController controller = new BookCopyController(
				request -> new FindBookCopyDetailsResponseDTO(BOOK_COPY, request.barcode(), "Clean Architecture",
						"Robert C. Martin", "", true, true, List.of()),
				MESSAGES);

		final FindBookCopyDetailsResponseDTO body = controller.find("UCO-0001", null).getBody();

		assertNotNull(body);
		assertEquals(List.of(), body.blocks());
	}

	@Test
	@DisplayName("pasa el usuario de la búsqueda para saber si el ejemplar apartado es suyo")
	void passesTheBorrower() {
		final UUID borrower = UUID.randomUUID();
		final List<UUID> received = new ArrayList<>();
		final BookCopyController controller = new BookCopyController(request -> {
			received.add(request.user());
			return new FindBookCopyDetailsResponseDTO(BOOK_COPY, request.barcode(), "Clean Architecture",
					"Robert C. Martin", "", true, true, List.of());
		}, MESSAGES);

		controller.find("UCO-0001", borrower);

		assertEquals(List.of(borrower), received);
	}

	@Test
	@DisplayName("si el ejemplar no existe deja pasar el error al manejador de errores")
	void letsNotFoundReachTheHandler() {
		final BookCopyController controller = new BookCopyController(request -> {
			throw BookHiveException.createConflict(MessageKey.BOOK_COPY_NOT_FOUND, "No existe.");
		}, MESSAGES);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> controller.find("UCO-9999", null));

		assertEquals(MessageKey.BOOK_COPY_NOT_FOUND, exception.getUserMessageKey());
	}

}
