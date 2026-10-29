package co.com.bookhive.payment.services.input.dto.request;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record PaymentEventRequestDTO(
		String reference,
		String transaction,
		String status,
		long amountInCents,
		List<String> signedValues,
		long timestamp,
		String checksum) {

	public PaymentEventRequestDTO {
		reference = TextHelper.getDefaultValue(reference);
		transaction = TextHelper.getDefaultValue(transaction);
		status = TextHelper.getDefaultValue(status);
		signedValues = List.copyOf(ObjectHelper.getDefaultValue(signedValues, List.<String>of()));
		checksum = TextHelper.getDefaultValue(checksum);
	}

}
