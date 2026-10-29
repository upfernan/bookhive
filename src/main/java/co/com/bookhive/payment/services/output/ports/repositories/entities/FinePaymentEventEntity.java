package co.com.bookhive.payment.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FinePaymentEventEntity(
		UUID id,
		UUID payment,
		String transaction,
		String status,
		long amountInCents,
		String source,
		Instant receivedAt) {

	public FinePaymentEventEntity {
		id = UUIDHelper.getDefaultValue(id);
		payment = UUIDHelper.getDefaultValue(payment);
		transaction = TextHelper.getDefaultValue(transaction).trim();
		status = TextHelper.getDefaultValue(status).trim();
		amountInCents = Math.max(amountInCents, 0L);
		source = TextHelper.getDefaultValue(source).trim();
		receivedAt = DateHelper.getDefaultValue(receivedAt);
	}

}
