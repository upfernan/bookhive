package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapperImpl;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.circulation.services.output.ports.AuditPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.usecase.lendbook.LendBookUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LendBookInputAdapterTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();
	private static final UUID LOAN = UUIDHelper.generateNew();
	private static final LocalDate DUE_DATE = LocalDate.of(2026, 11, 1);

	private FakeUseCase useCase;
	private List<String> audited;
	private LendBookInputAdapter inputAdapter;

	@BeforeEach
	void prepare() {
		useCase = new FakeUseCase();
		audited = new ArrayList<>();
		inputAdapter = new LendBookInputAdapter(useCase, new FakeSession(), new LendBookMapperImpl(),
				(action, institution, person, detail) -> audited.add(action + " " + institution + " " + person + " " + detail));
	}

	@Test
	@DisplayName("arma el dominio con los datos de la petición y de la sesión")
	void buildsDomainFromRequestAndSession() {
		final UUID bookCopy = UUIDHelper.generateNew();
		final UUID user = UUIDHelper.generateNew();

		inputAdapter.execute(new LendBookRequestDTO(bookCopy, user));

		assertEquals(bookCopy, useCase.received.getBookCopy());
		assertEquals(user, useCase.received.getUser());
		assertEquals(INSTITUTION, useCase.received.getInstitution());
		assertEquals(LIBRARIAN, useCase.received.getLibrarian());
		assertEquals(DateHelper.todayInUtc(), useCase.received.getLoanDate());
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
	@DisplayName("deja constancia del préstamo registrado con la institución y quien lo atendió")
	void auditsRegisteredLoan() {
		inputAdapter.execute(new LendBookRequestDTO(UUIDHelper.generateNew(), UUIDHelper.generateNew()));

		assertEquals(List.of(AuditPort.LOAN_REGISTERED + " " + INSTITUTION + " " + LIBRARIAN + " " + LOAN), audited);
	}

	@Test
	@DisplayName("si la petición llega vacía no llama al caso de uso")
	void rejectsNullRequest() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> inputAdapter.execute(null));

		assertEquals(MessageKey.LOAN_DATA_REQUIRED, exception.getUserMessageKey());
		assertNull(useCase.received);
		assertTrue(audited.isEmpty());
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

	private static final class FakeSession implements SessionContextPort {

		@Override
		public UUID getInstitution() {
			return INSTITUTION;
		}

		@Override
		public UUID getLibrarian() {
			return LIBRARIAN;
		}

	}

}
