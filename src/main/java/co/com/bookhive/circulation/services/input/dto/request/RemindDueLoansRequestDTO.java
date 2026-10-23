package co.com.bookhive.circulation.services.input.dto.request;

import java.time.LocalDate;

import co.com.bookhive.crosscutting.helper.DateHelper;

public record RemindDueLoansRequestDTO(LocalDate dueOn) {

	public static final String TASK = "recordar-vencimientos";

	public RemindDueLoansRequestDTO {
		dueOn = DateHelper.getDefaultValue(dueOn);
	}

}
