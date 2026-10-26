package co.com.bookhive.payment.services.output.ports.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record SettledFineEntity(boolean newlySettled, String paymentReference) {

	public SettledFineEntity {
		paymentReference = TextHelper.getDefaultValue(paymentReference);
	}

}
