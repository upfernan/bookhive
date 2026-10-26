package co.com.bookhive.payment.domain.finepayment.applypaymenttransaction;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record AppliedPaymentTransactionDomain(
		boolean known,
		String reference,
		String status,
		long amountInCents,
		boolean finePaid) {

	public AppliedPaymentTransactionDomain {
		reference = TextHelper.getDefaultValue(reference);
		status = TextHelper.getDefaultValue(status);
		amountInCents = Math.max(amountInCents, 0L);
	}

	public static AppliedPaymentTransactionDomain unknown(final String reference) {
		return new AppliedPaymentTransactionDomain(false, reference, "", 0L, false);
	}

}
