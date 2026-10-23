package co.com.bookhive.circulation.domain.loan.returnbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LoanStateTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 10, 23);

	@Test
	@DisplayName("el estado sale de los datos: sin devolución es activo o vencido según la fecha límite")
	void stateComesFromData() {
		assertEquals(LoanState.ACTIVE, LoanState.of(loanDueOn(TODAY), false, TODAY));
		assertEquals(LoanState.ACTIVE, LoanState.of(loanDueOn(TODAY.plusDays(3)), false, TODAY));
		assertEquals(LoanState.OVERDUE, LoanState.of(loanDueOn(TODAY.minusDays(1)), false, TODAY));
		assertEquals(LoanState.RETURNED, LoanState.of(loanDueOn(TODAY.minusDays(9)), true, TODAY));
		assertEquals(LoanEntity.STATUS_OVERDUE, LoanState.OVERDUE.label());
	}

	@Test
	@DisplayName("un préstamo activo o vencido pasa a devuelto")
	void openLoansCanBeReturned() {
		assertEquals(LoanState.RETURNED, LoanState.ACTIVE.returnBook());
		assertEquals(LoanState.RETURNED, LoanState.OVERDUE.returnBook());
	}

	@Test
	@DisplayName("un préstamo ya devuelto no se puede devolver otra vez")
	void returnedLoanCannotBeReturnedAgain() {
		final BookHiveException exception = assertThrows(BookHiveException.class, LoanState.RETURNED::returnBook);

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("sin préstamo o sin la fecha de hoy es un error inesperado")
	void requiresLoanAndToday() {
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> LoanState.of(null, false, TODAY)).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> LoanState.of(loanDueOn(TODAY), false, null)).getKind());
	}

	private static LoanEntity loanDueOn(final LocalDate dueDate) {
		final LoanEntity loan = new LoanEntity();
		loan.setDueDate(dueDate);
		return loan;
	}

}
