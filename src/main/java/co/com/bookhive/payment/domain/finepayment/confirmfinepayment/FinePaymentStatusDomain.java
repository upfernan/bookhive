package co.com.bookhive.payment.domain.finepayment.confirmfinepayment;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FinePaymentStatusDomain(String reference, String status, long amountInCents, boolean finePaid) {

	public FinePaymentStatusDomain {
		reference = TextHelper.getDefaultValue(reference);
		status = TextHelper.getDefaultValue(status);
		amountInCents = Math.max(amountInCents, 0L);
	}

}
