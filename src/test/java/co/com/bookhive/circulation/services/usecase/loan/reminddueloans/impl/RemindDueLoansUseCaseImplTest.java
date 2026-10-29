package co.com.bookhive.circulation.services.usecase.loan.reminddueloans.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
import co.com.bookhive.crosscutting.helper.DateHelper;

class RemindDueLoansUseCaseImplTest {

	private static final String ZONE = "America/Lima";
	private static final LocalDate DUE_ON = DateHelper.todayIn(ZoneId.of(ZONE)).plusDays(2);

	private final List<NotificationRequestEntity> notices = new ArrayList<>();
	private final FakeRepositories.Parameters parameters =
			new FakeRepositories.Parameters().with(15, 3, BigDecimal.ZERO, ZONE);

	@Test
	@DisplayName("pide un recordatorio por cada préstamo sin devolución que vence en los días pedidos, con la fecha de la institución")
	void remindsEveryDueLoan() {
		final LoanEntity loan = loan(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		final FakeRepositories.Loans loans = new FakeRepositories.Loans()
				.withActive(loan)
				.withActive(loan(UUID.randomUUID(), new UUID(0, 0), UUID.randomUUID()));

		final RemindDueLoansDomain result = new RemindDueLoansUseCaseImpl(parameters, loans, notices::add)
				.execute(new RemindDueLoansDomain(2, null, 0));

		assertEquals(1, result.reminded());
		assertEquals(DUE_ON, result.dueOn());
		assertEquals(DUE_ON, loans.searches().get(0).getDueDate());
		assertTrue(loans.searches().get(0).filtersByDueDate());
		assertTrue(loans.searches().get(0).onlyOpen());
		assertEquals(List.of(new NotificationRequestEntity(RemindDueLoansDomain.NOTICE_KIND, loan.getUser().getId(),
				loan.getBookCopy().getBook(), loan.getId(), DUE_ON)), notices);
	}

	@Test
	@DisplayName("sin pedido recuerda los que vencen hoy, y no se crea sin sus dependencias")
	void defaultsAndDependencies() {
		final FakeRepositories.Loans loans = new FakeRepositories.Loans();
		final RemindDueLoansUseCaseImpl useCase = new RemindDueLoansUseCaseImpl(parameters, loans, notices::add);

		assertEquals(DUE_ON.minusDays(2), useCase.execute(null).dueOn());
		assertThrows(BookHiveException.class, () -> new RemindDueLoansUseCaseImpl(null, loans, notices::add));
		assertThrows(BookHiveException.class, () -> new RemindDueLoansUseCaseImpl(parameters, null, notices::add));
		assertThrows(BookHiveException.class, () -> new RemindDueLoansUseCaseImpl(parameters, loans, null));
	}

	@Test
	@DisplayName("el dominio no acepta días negativos ni trabaja sin la fecha de hoy o de vencimiento")
	void domainRules() {
		final RemindDueLoansDomain domain = new RemindDueLoansDomain(-4, null, -1);

		assertEquals(0, domain.daysBefore());
		assertEquals(0, domain.reminded());
		assertThrows(BookHiveException.class, () -> domain.from(null));
		assertThrows(BookHiveException.class, domain::validate);
		assertEquals(DUE_ON, new RemindDueLoansDomain(2, null, 0).from(DUE_ON.minusDays(2)).dueOn());
	}

	@Test
	@DisplayName("el puerto de entrada pasa los días y devuelve cuántos recordatorios pidió")
	void inputAdapterReturnsCount() {
		final List<Integer> days = new ArrayList<>();
		final RemindDueLoansInputAdapter adapter = new RemindDueLoansInputAdapter(domain -> {
			days.add(domain.daysBefore());
			return domain.withReminded(3);
		});

		assertEquals(3, adapter.execute(new RemindDueLoansRequestDTO(2)).reminded());
		assertEquals(3, adapter.execute(null).reminded());
		assertEquals(List.of(2, 0), days);
		assertEquals(0, new RemindDueLoansRequestDTO(-3).daysBefore());
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
