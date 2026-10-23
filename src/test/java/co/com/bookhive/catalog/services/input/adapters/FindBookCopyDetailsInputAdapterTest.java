package co.com.bookhive.catalog.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.catalog.services.input.adapters.mapper.FindBookCopyDetailsMapperImpl;
import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.FindBookCopyDetailsUseCase;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyDetailsInputAdapterTest {

	private static final UUID BOOK_COPY = UUID.randomUUID();

	private FakeUseCase useCase;
	private FindBookCopyDetailsInputAdapter inputAdapter;

	@BeforeEach
	void prepare() {
		useCase = new FakeUseCase();
		inputAdapter = new FindBookCopyDetailsInputAdapter(useCase, new FindBookCopyDetailsMapperImpl());
	}

	@Test
	@DisplayName("convierte la petición en dominio y se la entrega al caso de uso")
	void buildsDomainFromRequest() {
		inputAdapter.execute(new FindBookCopyDetailsRequestDTO("UCO-0001"));

		assertEquals("UCO-0001", useCase.received.barcode());
	}

	@Test
	@DisplayName("devuelve el ejemplar con su título, con los bloqueos como claves de mensaje")
	void returnsDetails() {
		final FindBookCopyDetailsResponseDTO response =
				inputAdapter.execute(new FindBookCopyDetailsRequestDTO("UCO-0001"));

		assertEquals(BOOK_COPY, response.id());
		assertEquals("Clean Architecture", response.title());
		assertEquals(List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE), response.blocks());
	}

	private static final class FakeUseCase implements FindBookCopyDetailsUseCase {

		private FindBookCopyDetailsDomain received;

		@Override
		public BookCopyDetailsDomain execute(final FindBookCopyDetailsDomain input) {
			received = input;
			return new BookCopyDetailsDomain(BOOK_COPY, input.barcode(), "Clean Architecture", "Robert C. Martin",
					"", false, true, List.of(MessageKey.BOOK_COPY_NOT_AVAILABLE));
		}

	}

}
