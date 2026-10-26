package co.com.bookhive.payment.domain.finepayment.startfinepayment;

import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record StartFinePaymentDomain(UUID fine) {

	public StartFinePaymentDomain {
		fine = UUIDHelper.getDefaultValue(fine);
	}

	public void validate() {
		if (UUIDHelper.isDefaultValue(fine)) {
			throw BookHiveException.create(MessageKey.FINE_ID_REQUIRED, "Se pidió pagar una multa sin decir cuál.");
		}
	}

}
