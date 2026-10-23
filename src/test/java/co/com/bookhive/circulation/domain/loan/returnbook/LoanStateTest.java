package co.com.bookhive.circulation.domain.loan.returnbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LoanStateTest {

	@Test
	@DisplayName("un préstamo activo o vencido pasa a devuelto")
	void openLoansCanBeReturned() {
		assertEquals(LoanState.RETURNED, LoanState.of(LoanEntity.STATUS_ACTIVE).returnBook());
		assertEquals(LoanState.RETURNED, LoanState.of(LoanEntity.STATUS_OVERDUE).returnBook());
		assertEquals(LoanEntity.STATUS_RETURNED, LoanState.RETURNED.status());
	}

	@Test
	@DisplayName("un préstamo ya devuelto no se puede devolver otra vez")
	void returnedLoanCannotBeReturnedAgain() {
		final LoanState returned = LoanState.of(LoanEntity.STATUS_RETURNED);

		final BookHiveException exception = assertThrows(BookHiveException.class, returned::returnBook);

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("un estado desconocido es un error inesperado")
	void unknownStatusIsUnexpected() {
		final BookHiveException exception = assertThrows(BookHiveException.class, () -> LoanState.of("PERDIDO"));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

}
