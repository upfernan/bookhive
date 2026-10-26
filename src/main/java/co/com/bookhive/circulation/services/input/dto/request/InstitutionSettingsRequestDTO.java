package co.com.bookhive.circulation.services.input.dto.request;

import java.math.BigDecimal;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record InstitutionSettingsRequestDTO(int loanDays, int maxConcurrentLoans, BigDecimal dailyFineRate, String timeZone) {

	public InstitutionSettingsRequestDTO {
		dailyFineRate = ObjectHelper.getDefaultValue(dailyFineRate, BigDecimal.ZERO);
		timeZone = TextHelper.getDefaultValue(timeZone);
	}

}
