package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopyStatusEntity;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.crosscutting.messages.MessageKey;

class CirculationBookCopyStatusAdapterTest {

	@Test
	@DisplayName("le pregunta al módulo de préstamos por el ejemplar y trae su estado y sus bloqueos")
	void asksCirculationForBookCopy() {
		final UUID bookCopy = UUID.randomUUID();
		final UUID book = UUID.randomUUID();
		final List<FindBookCopyRequestDTO> requests = new ArrayList<>();
		final CirculationBookCopyStatusAdapter adapter = new CirculationBookCopyStatusAdapter(request -> {
			requests.add(request);
			return new FindBookCopyResponseDTO(bookCopy, book, request.barcode(), true, false,
					List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY));
		});

		final UUID borrower = UUID.randomUUID();

		final BookCopyStatusEntity status = adapter.findByBarcode("UCO-0001", borrower);

		assertEquals("UCO-0001", requests.get(0).barcode());
		assertEquals(borrower, requests.get(0).user());
		assertEquals(new BookCopyStatusEntity(bookCopy, book, "UCO-0001", true, false,
				List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY)), status);
	}

}
