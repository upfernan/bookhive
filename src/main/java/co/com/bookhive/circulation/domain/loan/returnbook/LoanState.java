package co.com.bookhive.circulation.domain.loan.returnbook;

import java.time.LocalDate;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public enum LoanState {

	ACTIVE(LoanEntity.STATUS_ACTIVE) {
		@Override
		public LoanState returnBook() {
			return RETURNED;
		}
	},

	OVERDUE(LoanEntity.STATUS_OVERDUE) {
		@Override
		public LoanState returnBook() {
			return RETURNED;
		}
	},

	RETURNED(LoanEntity.STATUS_RETURNED) {
		@Override
		public LoanState returnBook() {
			throw BookHiveException.createConflict(
					MessageKey.LOAN_RETURN_NOT_OPEN,
					"El préstamo ya tiene su devolución registrada y no puede devolverse otra vez.");
		}
	};

	private final String label;

	LoanState(final String label) {
		this.label = label;
	}

	public abstract LoanState returnBook();

	public String label() {
		return label;
	}

	public static LoanState of(final LoanEntity loan, final boolean returned, final LocalDate today) {
		if (returned) {
			return RETURNED;
		}
		if (ObjectHelper.isNull(loan) || DateHelper.isDefaultValue(today)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"Se pidió el estado de un préstamo sin el préstamo o sin la fecha de hoy.");
		}
		return loan.getDueDate().isBefore(today) ? OVERDUE : ACTIVE;
	}

}
