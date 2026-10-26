package co.com.bookhive.circulation.services.usecase.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters.UserHistoryRepositoryAdapter;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.UserHistoryJpaRepository;
import co.com.bookhive.circulation.services.input.adapters.FindUserAccountInputAdapter;
import co.com.bookhive.circulation.services.input.dto.response.UserAccountResponseDTO;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.UserHistoryRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReturnedLoanEntity;
import co.com.bookhive.circulation.services.usecase.account.finduseraccount.impl.FindUserAccountUseCaseImpl;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class FindUserAccountTest {

	private static final UUID USER = UUID.randomUUID();
	private static final UUID BOOK = UUID.randomUUID();
	private static final LocalDate TODAY = LocalDate.now(ZoneId.of("America/Bogota"));

	private final List<Integer> limits = new ArrayList<>();

	@Test
	@DisplayName("reúne préstamos con su multa acumulada a hoy, multas pendientes, historial y pagos")
	void gathersAccount() {
		final FakeRepositories.Loans loans = new FakeRepositories.Loans()
				.withActive(loan(TODAY.minusDays(1), TODAY.plusDays(10)))
				.withActive(loan(TODAY.minusDays(20), TODAY.minusDays(3)));
		final FakeRepositories.Fines fines = new FakeRepositories.Fines().withFine(fine(new BigDecimal("5000.00")));

		final UserAccountResponseDTO account = new FindUserAccountInputAdapter(useCase(loans, fines, history())).execute();

		assertEquals(TODAY.minusDays(3), account.activeLoans().get(0).dueDate());
		assertEquals(3, account.activeLoans().get(0).overdueDays());
		assertEquals(new BigDecimal("3000.00"), account.activeLoans().get(0).accruedFine());
		assertEquals(0, account.activeLoans().get(1).overdueDays());
		assertEquals(new BigDecimal("0.00"), account.activeLoans().get(1).accruedFine());
		assertEquals(BOOK, account.activeLoans().get(1).book());
		assertEquals(new BigDecimal("5000.00"), account.pendingFines().get(0).amount());
		assertEquals(new BigDecimal("5000.00"), account.pendingFinesTotal());
		assertEquals(new BigDecimal("3000.00"), account.accruingFinesTotal());
		assertEquals(TODAY.minusDays(5), account.returnedLoans().get(0).returnDate());
		assertEquals("uco-1", account.payments().get(0).reference());
		assertEquals(List.of(FindUserAccountUseCaseImpl.HISTORY_LIMIT, FindUserAccountUseCaseImpl.HISTORY_LIMIT), limits);
	}

	@Test
	@DisplayName("sin nada pendiente los totales quedan en cero")
	void emptyAccount() {
		final UserAccountResponseDTO account = new FindUserAccountInputAdapter(
				useCase(new FakeRepositories.Loans(), new FakeRepositories.Fines(), emptyHistory())).execute();

		assertEquals(new BigDecimal("0.00"), account.pendingFinesTotal());
		assertEquals(new BigDecimal("0.00"), account.accruingFinesTotal());
		assertEquals(List.of(), account.payments());
	}

	@Test
	@DisplayName("sin usuario en la sesión o sin dependencias es un error inesperado")
	void requiresSessionAndDependencies() {
		final FindUserAccountUseCaseImpl withoutUser = new FindUserAccountUseCaseImpl(session(null), parameters(),
				new FakeRepositories.Loans(), new FakeRepositories.Fines(), emptyHistory());
		final FakeRepositories.Parameters parameters = parameters();

		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class, withoutUser::execute).getKind());
		assertThrows(BookHiveException.class, () -> new FindUserAccountUseCaseImpl(null, parameters, null, null, null));
	}

	@Test
	@DisplayName("el historial pide a la base solo los últimos registros")
	void historyAdapterLimitsRows() {
		final List<Pageable> pages = new ArrayList<>();
		final UserHistoryJpaRepository jpa = (UserHistoryJpaRepository) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { UserHistoryJpaRepository.class }, (proxy, method, arguments) -> {
					pages.add((Pageable) arguments[1]);
					return List.of();
				});
		final UserHistoryRepositoryAdapter adapter = new UserHistoryRepositoryAdapter(jpa);

		adapter.findReturnedLoans(USER, 50);
		adapter.findPayments(USER, 0);

		assertEquals(50, pages.get(0).getPageSize());
		assertEquals(1, pages.get(1).getPageSize());
	}

	private FindUserAccountUseCaseImpl useCase(
			final FakeRepositories.Loans loans, final FakeRepositories.Fines fines, final UserHistoryRepository history) {
		return new FindUserAccountUseCaseImpl(session(USER), parameters(), loans, fines, history);
	}

	private UserHistoryRepository history() {
		return new UserHistoryRepository() {

			@Override
			public List<ReturnedLoanEntity> findReturnedLoans(final UUID user, final int limit) {
				limits.add(limit);
				return List.of(new ReturnedLoanEntity(UUID.randomUUID(), BOOK, TODAY.minusDays(30), TODAY.minusDays(15),
						TODAY.minusDays(5), 10));
			}

			@Override
			public List<FinePaymentEntity> findPayments(final UUID user, final int limit) {
				limits.add(limit);
				return List.of(new FinePaymentEntity(UUID.randomUUID(), BOOK, "uco-1", new BigDecimal("10000.00"),
						Instant.parse("2026-10-20T15:00:00Z")));
			}

		};
	}

	private static UserHistoryRepository emptyHistory() {
		return new UserHistoryRepository() {

			@Override
			public List<ReturnedLoanEntity> findReturnedLoans(final UUID user, final int limit) {
				return List.of();
			}

			@Override
			public List<FinePaymentEntity> findPayments(final UUID user, final int limit) {
				return List.of();
			}

		};
	}

	private static FakeRepositories.Parameters parameters() {
		return new FakeRepositories.Parameters().with(15, 3, new BigDecimal("1000"));
	}

	private static LoanEntity loan(final LocalDate loanDate, final LocalDate dueDate) {
		final BookCopyEntity copy = new BookCopyEntity();
		copy.setId(UUID.randomUUID());
		copy.setBook(BOOK);
		final LoanEntity loan = new LoanEntity();
		loan.setId(UUID.randomUUID());
		loan.setBookCopy(copy);
		loan.setLoanDate(loanDate);
		loan.setDueDate(dueDate);
		return loan;
	}

	private static FineEntity fine(final BigDecimal amount) {
		final FineEntity fine = new FineEntity();
		fine.setId(UUID.randomUUID());
		fine.setLoan(loan(TODAY.minusDays(40), TODAY.minusDays(25)));
		fine.setIssueDate(TODAY.minusDays(20));
		fine.setOverdueDays(5);
		fine.setDailyRate(new BigDecimal("1000.00"));
		fine.setTotalAmount(amount);
		return fine;
	}

	private static SessionContextPort session(final UUID user) {
		return new SessionContextPort() {

			@Override
			public UUID getInstitution() {
				return UUID.randomUUID();
			}

			@Override
			public UUID getLibrarian() {
				return user;
			}

		};
	}

}
