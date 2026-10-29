package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindBookCopyMapperImpl;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.FindBookCopyUseCase;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyInputAdapterTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();
	private static final UUID BOOK = UUIDHelper.generateNew();

	private FakeUseCase useCase;
	private FindBookCopyInputAdapter inputAdapter;

	@BeforeEach
	void prepare() {
		useCase = new FakeUseCase();
		inputAdapter = new FindBookCopyInputAdapter(useCase, new FindBookCopyMapperImpl());
	}

	@Test
	@DisplayName("convierte la petición en dominio y se la entrega al caso de uso")
	void buildsDomainFromRequest() {
		inputAdapter.execute(new FindBookCopyRequestDTO("UCO-0001"));

		assertEquals("UCO-0001", useCase.received.getBarcode());
	}

	@Test
	@DisplayName("devuelve lo que encontró el caso de uso, con los bloqueos como claves de mensaje")
	void returnsFoundBookCopy() {
		final FindBookCopyResponseDTO response = inputAdapter.execute(new FindBookCopyRequestDTO("UCO-0001"));

		assertEquals(BOOK_COPY, response.id());
		assertEquals(BOOK, response.book());
		assertEquals(List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY), response.blocks());
	}

	private static final class FakeUseCase implements FindBookCopyUseCase {

		private FindBookCopyDomain received;

		@Override
		public FoundBookCopyDomain execute(final FindBookCopyDomain input) {
			received = input;
			return new FoundBookCopyDomain(BOOK_COPY, BOOK, input.getBarcode(), true, false,
					List.of(MessageKey.BOOK_COPY_REFERENCE_ONLY));
		}

	}

}
