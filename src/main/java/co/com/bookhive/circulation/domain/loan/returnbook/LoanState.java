package co.com.bookhive.circulation.domain.loan.returnbook;

import java.util.Arrays;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
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
					"El préstamo ya está en estado " + status() + " y no puede devolverse otra vez.");
		}
	};

	private final String status;

	LoanState(final String status) {
		this.status = status;
	}

	public abstract LoanState returnBook();

	public String status() {
		return status;
	}

	public static LoanState of(final String status) {
		return Arrays.stream(values())
				.filter(state -> state.status.equals(status))
				.findFirst()
				.orElseThrow(() -> BookHiveException.createUnexpected(
						MessageKey.LOAN_UNEXPECTED,
						"El préstamo tiene un estado desconocido: " + status + "."));
	}

}
