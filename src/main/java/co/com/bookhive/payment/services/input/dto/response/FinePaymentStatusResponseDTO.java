package co.com.bookhive.payment.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FinePaymentStatusResponseDTO(String reference, String status, long amountInCents, boolean finePaid) {

	public FinePaymentStatusResponseDTO {
		reference = TextHelper.getDefaultValue(reference);
		status = TextHelper.getDefaultValue(status);
	}

}
