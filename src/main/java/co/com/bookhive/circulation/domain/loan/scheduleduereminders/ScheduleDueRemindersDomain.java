package co.com.bookhive.circulation.domain.loan.scheduleduereminders;

import java.util.List;

import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record ScheduleDueRemindersDomain(List<String> institutions) {

	public static final String TASK = "recordar-vencimientos";

	public ScheduleDueRemindersDomain {
		institutions = List.copyOf(ObjectHelper.getDefaultValue(institutions, List.of()));
	}

}
