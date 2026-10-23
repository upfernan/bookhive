package co.com.bookhive.circulation.services.usecase.loan.reminddueloans.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.reminddueloans.RemindDueLoansDomain;
import co.com.bookhive.circulation.services.input.adapters.RemindDueLoansInputAdapter;
import co.com.bookhive.circulation.services.input.dto.request.RemindDueLoansRequestDTO;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class RemindDueLoansUseCaseImplTest {

	private static final LocalDate DUE_ON = LocalDate.of(2026, 10, 24);

	private final List<NotificationRequestEntity> notices = new ArrayList<>();

	@Test
	@DisplayName("pide un recordatorio por cada préstamo activo que vence ese día")
	void remindsEveryDueLoan() {
		final LoanEntity loan = loan(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		final FakeRepositories.Loans loans = new FakeRepositories.Loans()
				.withActive(loan)
				.withActive(loan(UUID.randomUUID(), new UUID(0, 0), UUID.randomUUID()));

		final RemindDueLoansDomain result =
				new RemindDueLoansUseCaseImpl(loans, notices::add).execute(new RemindDueLoansDomain(DUE_ON, 0));

		assertEquals(1, result.reminded());
		assertEquals(DUE_ON, loans.searches().get(0).getDueDate());
		assertTrue(loans.searches().get(0).filtersByDueDate());
		assertEquals(List.of(new NotificationRequestEntity(RemindDueLoansDomain.NOTICE_KIND, loan.getUser().getId(),
				loan.getBookCopy().getBook(), loan.getId(), DUE_ON)), notices);
	}

	@Test
	@DisplayName("sin fecha no busca préstamos y no se crea sin sus dependencias")
	void validatesDateAndDependencies() {
		final FakeRepositories.Loans loans = new FakeRepositories.Loans();
		final RemindDueLoansUseCaseImpl useCase = new RemindDueLoansUseCaseImpl(loans, notices::add);

		assertThrows(BookHiveException.class, () -> useCase.execute(null));
		assertTrue(loans.searches().isEmpty());
		assertThrows(BookHiveException.class, () -> new RemindDueLoansUseCaseImpl(null, notices::add));
		assertThrows(BookHiveException.class, () -> new RemindDueLoansUseCaseImpl(loans, null));
	}

	@Test
	@DisplayName("el puerto de entrada devuelve cuántos recordatorios pidió")
	void inputAdapterReturnsCount() {
		final RemindDueLoansInputAdapter adapter =
				new RemindDueLoansInputAdapter(domain -> domain.withReminded(3));

		final RemindDueLoansInputAdapter realAdapter = new RemindDueLoansInputAdapter(
				new RemindDueLoansUseCaseImpl(new FakeRepositories.Loans(), notices::add));

		assertEquals(3, adapter.execute(new RemindDueLoansRequestDTO(DUE_ON)).reminded());
		assertThrows(BookHiveException.class, () -> realAdapter.execute(null));
	}

	private static LoanEntity loan(final UUID id, final UUID user, final UUID book) {
		final UserEntity borrower = new UserEntity();
		borrower.setId(user);
		final BookCopyEntity copy = new BookCopyEntity();
		copy.setBook(book);
		final LoanEntity loan = new LoanEntity();
		loan.setId(id);
		loan.setUser(borrower);
		loan.setBookCopy(copy);
		loan.setDueDate(DUE_ON);
		return loan;
	}

}
