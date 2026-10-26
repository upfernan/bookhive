package co.com.bookhive.circulation.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record SettleFineResponseDTO(boolean newlySettled, String paymentReference) {

	public SettleFineResponseDTO {
		paymentReference = TextHelper.getDefaultValue(paymentReference);
	}

}
