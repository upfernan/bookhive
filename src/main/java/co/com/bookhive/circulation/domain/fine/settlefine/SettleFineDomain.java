package co.com.bookhive.circulation.domain.fine.settlefine;

import java.math.BigDecimal;
import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record SettleFineDomain(UUID fine, String paymentReference, BigDecimal amount) {

	public static final String NOTICE_KIND = "MULTA_PAGADA";

	public SettleFineDomain {
		fine = UUIDHelper.getDefaultValue(fine);
		paymentReference = TextHelper.getDefaultValue(paymentReference).trim();
		amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
	}

	public void validate() {
		if (UUIDHelper.isDefaultValue(fine) || TextHelper.isDefaultValue(paymentReference)) {
			throw BookHiveException.create(
					MessageKey.FINE_PAYMENT_DATA_REQUIRED,
					"Se pidió saldar una multa sin la multa o sin la referencia del pago.");
		}
		if (amount.signum() <= 0) {
			throw BookHiveException.create(
					MessageKey.FINE_PAYMENT_DATA_REQUIRED,
					"Se pidió saldar la multa " + fine + " con un valor de " + amount + ".");
		}
	}

	public boolean coversExactly(final BigDecimal fineAmount) {
		return amount.compareTo(ObjectHelper.getDefaultValue(fineAmount, BigDecimal.ZERO)) == 0;
	}

}
