package co.com.bookhive.payment.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FinePaymentEntity(
		UUID id,
		String reference,
		UUID fine,
		UUID payer,
		UUID institution,
		long amountInCents,
		String currency,
		Instant expiresAt,
		Instant createdAt) {

	public FinePaymentEntity {
		id = UUIDHelper.getDefaultValue(id);
		reference = TextHelper.getDefaultValue(reference).trim();
		fine = UUIDHelper.getDefaultValue(fine);
		payer = UUIDHelper.getDefaultValue(payer);
		institution = UUIDHelper.getDefaultValue(institution);
		amountInCents = Math.max(amountInCents, 0L);
		currency = TextHelper.getDefaultValue(currency).trim();
		expiresAt = DateHelper.getDefaultValue(expiresAt);
		createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
