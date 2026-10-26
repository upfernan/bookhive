package co.com.bookhive.payment.services.output.ports.entities;

import java.time.Instant;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record CheckoutRequestEntity(String reference, long amountInCents, String currency, Instant expiresAt) {

	public CheckoutRequestEntity {
		reference = TextHelper.getDefaultValue(reference);
		amountInCents = Math.max(amountInCents, 0L);
		currency = TextHelper.getDefaultValue(currency);
		expiresAt = DateHelper.getDefaultValue(expiresAt);
	}

}
