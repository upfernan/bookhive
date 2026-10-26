package co.com.bookhive.payment.services.output.ports.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record GatewayTransactionEntity(String id, String reference, String status, long amountInCents) {

	public GatewayTransactionEntity {
		id = TextHelper.getDefaultValue(id);
		reference = TextHelper.getDefaultValue(reference);
		status = TextHelper.getDefaultValue(status);
		amountInCents = Math.max(amountInCents, 0L);
	}

}
