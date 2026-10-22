package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapperImpl;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.LendBookUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LendBookInputAdapterTest {

	private static final UUID LOAN = UUIDHelper.generateNew();
	private static final LocalDate DUE_DATE = LocalDate.of(2026, 11, 1);

	private FakeUseCase useCase;
	private LendBookInputAdapter inputAdapter;

	@BeforeEach
	void prepare() {
		useCase = new FakeUseCase();
		inputAdapter = new LendBookInputAdapter(useCase, new LendBookMapperImpl());
	}

	@Test
	@DisplayName("convierte la petición en dominio y se la entrega al caso de uso")
	void buildsDomainFromRequest() {
		final UUID bookCopy = UUIDHelper.generateNew();
		final UUID user = UUIDHelper.generateNew();

		inputAdapter.execute(new LendBookRequestDTO(bookCopy, user));

		assertEquals(bookCopy, useCase.received.getBookCopy());
		assertEquals(user, useCase.received.getUser());
		assertEquals(DateHelper.todayInUtc(), useCase.received.getLoanDate());
	}

	@Test
	@DisplayName("no llena la institución ni el bibliotecario: eso lo hace el caso de uso con la sesión")
	void leavesSessionDataToUseCase() {
		inputAdapter.execute(new LendBookRequestDTO(UUIDHelper.generateNew(), UUIDHelper.generateNew()));

		assertEquals(UUIDHelper.getDefaultValue(), useCase.received.getInstitution());
		assertEquals(UUIDHelper.getDefaultValue(), useCase.received.getLibrarian());
	}

	@Test
	@DisplayName("devuelve el número del préstamo y la fecha de entrega que registró el caso de uso")
	void returnsRegisteredLoan() {
		final LendBookResponseDTO response = inputAdapter.execute(
				new LendBookRequestDTO(UUIDHelper.generateNew(), UUIDHelper.generateNew()));

		assertEquals(LOAN, response.getLoan());
		assertEquals(DUE_DATE, response.getDueDate());
	}

	@Test
	@DisplayName("si la petición llega vacía no llama al caso de uso")
	void rejectsNullRequest() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> inputAdapter.execute(null));

		assertEquals(MessageKey.LOAN_DATA_REQUIRED, exception.getUserMessageKey());
		assertNull(useCase.received);
	}

	private static final class FakeUseCase implements LendBookUseCase {

		private LendBookDomain received;

		@Override
		public LendBookDomain execute(final LendBookDomain input) {
			received = input;
			return new LendBookDomain(LOAN, input.getBookCopy(), input.getUser(), input.getInstitution(),
					input.getLibrarian(), input.getLoanDate(), DUE_DATE);
		}

	}

}
