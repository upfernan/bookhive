package co.com.bookhive.circulation.domain.institution.configureinstitution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.ZoneId;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record InstitutionSettingsDomain(int loanDays, int maxConcurrentLoans, BigDecimal dailyFineRate, String timeZone) {

	public static final int MIN_LOAN_DAYS = 1;
	public static final int MAX_LOAN_DAYS = 90;
	public static final int MIN_CONCURRENT_LOANS = 1;
	public static final int MAX_CONCURRENT_LOANS = 20;
	public static final BigDecimal MAX_DAILY_FINE_RATE = new BigDecimal("1000000");

	public InstitutionSettingsDomain {
		dailyFineRate = ObjectHelper.getDefaultValue(dailyFineRate, BigDecimal.ZERO);
		timeZone = TextHelper.getDefaultValue(timeZone).trim();
	}

	public void validate() {
		if (loanDays < MIN_LOAN_DAYS || loanDays > MAX_LOAN_DAYS) {
			throw BookHiveException.create(
					MessageKey.INSTITUTION_SETTINGS_LOAN_DAYS_INVALID, "Se pidieron " + loanDays + " días de préstamo.");
		}
		if (maxConcurrentLoans < MIN_CONCURRENT_LOANS || maxConcurrentLoans > MAX_CONCURRENT_LOANS) {
			throw BookHiveException.create(
					MessageKey.INSTITUTION_SETTINGS_MAX_LOANS_INVALID,
					"Se pidió un máximo de " + maxConcurrentLoans + " préstamos simultáneos.");
		}
		if (dailyFineRate.signum() < 0
				|| dailyFineRate.compareTo(MAX_DAILY_FINE_RATE) > 0
				|| dailyFineRate.stripTrailingZeros().scale() > 2) {
			throw BookHiveException.create(
					MessageKey.INSTITUTION_SETTINGS_FINE_RATE_INVALID, "Se pidió una tarifa diaria de " + dailyFineRate + ".");
		}
		if (!isValidZone(timeZone)) {
			throw BookHiveException.create(
					MessageKey.INSTITUTION_SETTINGS_TIME_ZONE_INVALID, "Se pidió la zona horaria \"" + timeZone + "\".");
		}
	}

	public BigDecimal normalizedDailyFineRate() {
		return dailyFineRate.setScale(2, RoundingMode.UNNECESSARY);
	}

	private static boolean isValidZone(final String zone) {
		if (null == zone || !zone.contains("/")) {
			return false;
		}
		try {
			ZoneId.of(zone);
			return true;
		} catch (final DateTimeException _) {
			return false;
		}
	}

}
