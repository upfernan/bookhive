package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FineSettlementEntity(UUID id, UUID fine, String paymentReference, BigDecimal amount, Instant settledAt) {

	public FineSettlementEntity {
		id = UUIDHelper.getDefaultValue(id);
		fine = UUIDHelper.getDefaultValue(fine);
		paymentReference = TextHelper.getDefaultValue(paymentReference).trim();
		amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
		settledAt = DateHelper.getDefaultValue(settledAt);
	}

}
