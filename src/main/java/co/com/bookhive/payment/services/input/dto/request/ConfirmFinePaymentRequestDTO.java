package co.com.bookhive.payment.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record ConfirmFinePaymentRequestDTO(String reference, String transaction) {

	public ConfirmFinePaymentRequestDTO {
		reference = TextHelper.getDefaultValue(reference);
		transaction = TextHelper.getDefaultValue(transaction);
	}

}
