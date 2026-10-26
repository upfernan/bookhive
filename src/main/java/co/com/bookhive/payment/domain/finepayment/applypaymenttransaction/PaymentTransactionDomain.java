package co.com.bookhive.payment.domain.finepayment.applypaymenttransaction;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.payment.domain.finepayment.PaymentStatus;

public record PaymentTransactionDomain(
		String reference,
		String transaction,
		String status,
		long amountInCents,
		String source) {

	public static final String SOURCE_EVENT = "EVENTO";
	public static final String SOURCE_QUERY = "CONSULTA";
	public static final String SOURCE_RECONCILIATION = "CONCILIACION";

	public PaymentTransactionDomain {
		reference = TextHelper.getDefaultValue(reference).trim();
		transaction = TextHelper.getDefaultValue(transaction).trim();
		status = PaymentStatus.normalize(status);
		amountInCents = Math.max(amountInCents, 0L);
		source = TextHelper.getDefaultValue(source);
	}

	public boolean isComplete() {
		return !TextHelper.isDefaultValue(reference)
				&& !TextHelper.isDefaultValue(transaction)
				&& PaymentStatus.isKnown(status);
	}

}
