package co.com.bookhive.circulation.domain.loan.reminddueloans;

import java.time.LocalDate;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record RemindDueLoansDomain(LocalDate dueOn, int reminded) {

	public static final String NOTICE_KIND = "PRESTAMO_POR_VENCER";

	public RemindDueLoansDomain {
		dueOn = DateHelper.getDefaultValue(dueOn);
		reminded = Math.max(reminded, 0);
	}

	public void validate() {
		if (DateHelper.isDefaultValue(dueOn)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"La tarea de recordatorios llegó sin la fecha de vencimiento a recordar.");
		}
	}

	public RemindDueLoansDomain withReminded(final int count) {
		return new RemindDueLoansDomain(dueOn, count);
	}

}
