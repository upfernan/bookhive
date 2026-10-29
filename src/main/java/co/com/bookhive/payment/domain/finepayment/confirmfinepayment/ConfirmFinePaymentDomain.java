package co.com.bookhive.payment.domain.finepayment.confirmfinepayment;

import java.util.regex.Pattern;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record ConfirmFinePaymentDomain(String reference, String transaction) {

	private static final Pattern TRANSACTION_PATTERN = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

	public ConfirmFinePaymentDomain {
		reference = TextHelper.getDefaultValue(reference).trim();
		transaction = TextHelper.getDefaultValue(transaction).trim();
	}

	public void validate() {
		if (TextHelper.isDefaultValue(reference)) {
			throw BookHiveException.create(MessageKey.PAYMENT_REFERENCE_REQUIRED, "Se pidió confirmar un pago sin referencia.");
		}
		if (!TRANSACTION_PATTERN.matcher(transaction).matches()) {
			throw BookHiveException.create(
					MessageKey.PAYMENT_TRANSACTION_REQUIRED,
					"Se pidió confirmar el pago " + reference + " sin una transacción válida.");
		}
	}

}
