package co.com.bookhive.circulation.services.usecase.loan.returnbook.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.returnbook.ReturnBookDomain;
import co.com.bookhive.circulation.domain.loan.returnbook.ReturnedLoanDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class ReturnBookUseCaseImplTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID BOOK_COPY = UUID.randomUUID();
	private static final UUID USER = UUID.randomUUID();
	private static final UUID LOAN = UUID.randomUUID();
	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final UUID LIBRARIAN = UUID.randomUUID();
	private static final LocalDate TODAY = DateHelper.todayIn(ZoneId.of(InstitutionParametersEntity.DEFAULT_TIME_ZONE));

	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Loans loans;
	private FakeRepositories.LoanReturns loanReturns;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Parameters parameters;
	private List<String> audited;
	private List<NotificationRequestEntity> notices;

	@BeforeEach
	void prepare() {
		final BookCopyEntity copy = FakeRepositories.bookCopyOf(BOOK, BookCopyEntity.STATUS_LENT);
		copy.setId(BOOK_COPY);
		bookCopies = new FakeRepositories.BookCopies().with(copy);
		loans = new FakeRepositories.Loans();
		loanReturns = new FakeRepositories.LoanReturns();
		fines = new FakeRepositories.Fines();
		parameters = new FakeRepositories.Parameters().with(15, 3, new BigDecimal("1000"));
		audited = new ArrayList<>();
		notices = new ArrayList<>();
	}

	@Test
	@DisplayName("una devolución a tiempo cierra el préstamo, libera el ejemplar y no crea multa")
	void onTimeReturnHasNoFine() {
		loans.withActive(loan(LoanEntity.STATUS_ACTIVE, TODAY.plusDays(2)));

		final ReturnedLoanDomain returned = useCase().execute(request());

		assertEquals(LOAN, returned.loan());
		assertEquals(0, returned.overdueDays());
		assertFalse(returned.hasFine());
		assertEquals(BigDecimal.ZERO, returned.fineAmount());
		assertEquals(TODAY, returned.returnDate());
		assertEquals(BOOK_COPY, returned.bookCopy());
		assertEquals(LOAN, loanReturns.created().get(0).getLoan());
		assertEquals(List.of(BOOK_COPY + ":PRESTADO->DISPONIBLE"), bookCopies.statusChanges());
		assertTrue(bookCopies.updated().isEmpty());
		assertTrue(fines.created().isEmpty());
		assertTrue(notices.isEmpty());
		assertEquals(List.of(AuditPort.LOAN_RETURNED + " " + INSTITUTION + " " + LIBRARIAN + " " + LOAN), audited);
	}

	@Test
	@DisplayName("una devolución tardía registra la devolución, crea la multa con la tarifa congelada y avisa al lector")
	void lateReturnCreatesFineAndNotice() {
		final LocalDate dueDate = TODAY.minusDays(3);
		loans.withActive(loan(LoanEntity.STATUS_OVERDUE, dueDate));

		final ReturnedLoanDomain returned = useCase().execute(request());

		final LoanReturnEntity loanReturn = loanReturns.created().get(0);
		assertEquals(LOAN, loanReturn.getLoan());
		assertEquals(3, loanReturn.getOverdueDays());
		assertEquals(LIBRARIAN, loanReturn.getLibrarian());
		assertEquals(TODAY, loanReturn.getReturnDate());

		final FineEntity fine = fines.created().get(0);
		assertEquals(3, fine.getOverdueDays());
		assertEquals(new BigDecimal("1000.00"), fine.getDailyRate());
		assertEquals(new BigDecimal("3000.00"), fine.getTotalAmount());
		assertEquals(FineEntity.STATUS_PENDING, fine.getStatus());
		assertEquals(USER, fine.getUser().getId());
		assertEquals(TODAY, fine.getIssueDate());

		assertTrue(returned.hasFine());
		assertEquals(fine.getId(), returned.fine());
		assertEquals(new BigDecimal("3000.00"), returned.fineAmount());
		assertEquals(new BigDecimal("1000.00"), returned.dailyRate());
		assertEquals(List.of(new NotificationRequestEntity(ReturnBookDomain.NOTICE_KIND, USER, BOOK, LOAN, dueDate)),
				notices);
		assertEquals(List.of(AuditPort.LOAN_RETURNED + " " + INSTITUTION + " " + LIBRARIAN + " " + LOAN + " multa " + fine.getId()),
				audited);
	}

	@Test
	@DisplayName("sin préstamo abierto no registra nada")
	void withoutOpenLoanNothingIsSaved() {
		final ReturnBookUseCaseImpl useCase = useCase();
		final ReturnBookDomain request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN, exception.getUserMessageKey());
		assertTrue(loanReturns.created().isEmpty());
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("si el mismo ejemplar se devuelve otra vez el mismo día, responde lo ya registrado sin repetir nada")
	void repeatedReturnAnswersTheSame() {
		final LoanEntity loan = loan(LoanEntity.STATUS_ACTIVE, TODAY.minusDays(3));
		loans.withReturned(loan);
		loanReturns.with(loanReturn(TODAY, 3));
		fines.withFine(fineOf(loan));

		final ReturnedLoanDomain returned = useCase().execute(request());

		assertEquals(LOAN, returned.loan());
		assertEquals(3, returned.overdueDays());
		assertEquals(new BigDecimal("3000.00"), returned.fineAmount());
		assertEquals(new BigDecimal("1000.00"), returned.dailyRate());
		assertTrue(returned.hasFine());
		assertTrue(loanReturns.created().isEmpty());
		assertTrue(fines.created().isEmpty());
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(audited.isEmpty());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("si otra petición ya devolvió el ejemplar, responde esa devolución; si no la hay, se rechaza")
	void concurrentReturnAnswersTheOtherOne() {
		final LoanEntity loan = loan(LoanEntity.STATUS_ACTIVE, TODAY.plusDays(1));
		loans.withActive(loan);
		bookCopies.changedByOtherRequest();
		final ReturnBookUseCaseImpl withoutOther = useCase();
		final ReturnBookDomain request = request();

		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN,
				assertThrows(BookHiveException.class, () -> withoutOther.execute(request)).getUserMessageKey());

		loanReturns.with(loanReturn(TODAY, 0));
		final ReturnedLoanDomain returned = useCase().execute(request());

		assertEquals(LOAN, returned.loan());
		assertFalse(returned.hasFine());
		assertEquals(BigDecimal.ZERO, returned.fineAmount());
		assertTrue(loanReturns.created().isEmpty());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("una devolución de otro día no cuenta como repetida")
	void oldReturnIsNotARepeat() {
		loans.withReturned(loan(LoanEntity.STATUS_ACTIVE, TODAY.minusDays(10)));
		loanReturns.with(loanReturn(TODAY.minusDays(1), 9));
		final ReturnBookUseCaseImpl useCase = useCase();
		final ReturnBookDomain request = request();

		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN,
				assertThrows(BookHiveException.class, () -> useCase.execute(request)).getUserMessageKey());
	}

	@Test
	@DisplayName("un dominio nulo o una dependencia faltante se rechazan")
	void rejectsNullDomainAndDependencies() {
		final ReturnBookUseCaseImpl useCase = useCase();
		final FakeSession session = new FakeSession();

		assertEquals(MessageKey.LOAN_DATA_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());
		assertThrows(BookHiveException.class, () -> new ReturnBookUseCaseImpl(
				parameters, bookCopies, loans, null, fines, session, this::audit, notices::add));
	}

	private ReturnBookUseCaseImpl useCase() {
		return new ReturnBookUseCaseImpl(parameters, bookCopies, loans, loanReturns, fines, new FakeSession(),
				this::audit, notices::add);
	}

	private static ReturnBookDomain request() {
		return new ReturnBookDomain(BOOK_COPY, null, null, null);
	}

	private static LoanReturnEntity loanReturn(final LocalDate returnDate, final int overdueDays) {
		final LoanReturnEntity loanReturn = new LoanReturnEntity();
		loanReturn.setId(UUID.randomUUID());
		loanReturn.setLoan(LOAN);
		loanReturn.setReturnDate(returnDate);
		loanReturn.setOverdueDays(overdueDays);
		loanReturn.setLibrarian(LIBRARIAN);
		return loanReturn;
	}

	private static FineEntity fineOf(final LoanEntity loan) {
		final FineEntity fine = new FineEntity();
		fine.setId(UUID.randomUUID());
		fine.setLoan(loan);
		fine.setOverdueDays(3);
		fine.setDailyRate(new BigDecimal("1000.00"));
		fine.setTotalAmount(new BigDecimal("3000.00"));
		return fine;
	}

	private static LoanEntity loan(final String status, final LocalDate dueDate) {
		final LoanEntity loan = new LoanEntity();
		loan.setId(LOAN);
		loan.setStatus(status);
		loan.setDueDate(dueDate);
		loan.setLoanDate(dueDate.minusDays(15));
		loan.setUser(FakeRepositories.user(USER, "Ana Pérez", "ana@uco.edu.co"));
		return loan;
	}

	private void audit(final String action, final UUID institution, final UUID person, final String detail) {
		audited.add(action + " " + institution + " " + person + " " + detail);
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
