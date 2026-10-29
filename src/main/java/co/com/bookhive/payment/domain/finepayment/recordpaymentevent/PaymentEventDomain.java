package co.com.bookhive.payment.domain.finepayment.recordpaymentevent;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record PaymentEventDomain(
		String reference,
		String transaction,
		String status,
		long amountInCents,
		List<String> signedValues,
		long timestamp,
		String checksum) {

	public PaymentEventDomain {
		reference = TextHelper.getDefaultValue(reference);
		transaction = TextHelper.getDefaultValue(transaction);
		status = TextHelper.getDefaultValue(status);
		amountInCents = Math.max(amountInCents, 0L);
		signedValues = List.copyOf(ObjectHelper.getDefaultValue(signedValues, List.<String>of()));
		timestamp = Math.max(timestamp, 0L);
		checksum = TextHelper.getDefaultValue(checksum).trim();
	}

}
