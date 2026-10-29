package co.com.bookhive.circulation.domain.institution.configureinstitution;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record ParameterChange(String parameter, String previousValue, String newValue) {

	public static final String LOAN_DAYS = "loan_days";
	public static final String MAX_CONCURRENT_LOANS = "max_concurrent_loans";
	public static final String DAILY_FINE_RATE = "daily_fine_rate";
	public static final String TIME_ZONE = "time_zone";

	public ParameterChange {
		parameter = TextHelper.getDefaultValue(parameter);
		previousValue = TextHelper.getDefaultValue(previousValue);
		newValue = TextHelper.getDefaultValue(newValue);
	}

	public static List<ParameterChange> between(
			final InstitutionSettingsDomain current, final InstitutionSettingsDomain requested) {

		final List<ParameterChange> changes = new ArrayList<>();
		addIfDifferent(changes, LOAN_DAYS, current.loanDays(), requested.loanDays());
		addIfDifferent(changes, MAX_CONCURRENT_LOANS, current.maxConcurrentLoans(), requested.maxConcurrentLoans());
		addIfDifferent(changes, DAILY_FINE_RATE,
				current.normalizedDailyFineRate().toPlainString(), requested.normalizedDailyFineRate().toPlainString());
		addIfDifferent(changes, TIME_ZONE, current.timeZone(), requested.timeZone());
		return List.copyOf(changes);
	}

	public String describe() {
		return parameter + " " + previousValue + "->" + newValue;
	}

	private static void addIfDifferent(
			final List<ParameterChange> changes, final String parameter, final Object previous, final Object requested) {

		if (!Objects.equals(previous, requested)) {
			changes.add(new ParameterChange(parameter, String.valueOf(previous), String.valueOf(requested)));
		}
	}

}
