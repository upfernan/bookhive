package co.com.bookhive.circulation.services.input.dto.response;

import java.math.BigDecimal;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record InstitutionSettingsResponseDTO(
		int loanDays,
		int maxConcurrentLoans,
		BigDecimal dailyFineRate,
		String timeZone) {

	public InstitutionSettingsResponseDTO {
		dailyFineRate = ObjectHelper.getDefaultValue(dailyFineRate, BigDecimal.ZERO);
		timeZone = TextHelper.getDefaultValue(timeZone);
	}

}
