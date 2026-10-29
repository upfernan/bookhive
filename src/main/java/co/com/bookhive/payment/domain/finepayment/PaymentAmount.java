package co.com.bookhive.payment.domain.finepayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class PaymentAmount {

	public static final String CURRENCY = "COP";

	private PaymentAmount() {
		super();
	}

	private static final BigDecimal MAXIMUM = BigDecimal.valueOf(Long.MAX_VALUE, 2);

	public static boolean isPayable(final BigDecimal amount) {
		final BigDecimal safeAmount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
		return safeAmount.signum() > 0
				&& safeAmount.stripTrailingZeros().scale() <= 2
				&& safeAmount.compareTo(MAXIMUM) <= 0;
	}

	public static long inCents(final BigDecimal amount) {
		final BigDecimal safeAmount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
		return safeAmount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
	}

	public static BigDecimal fromCents(final long amountInCents) {
		return BigDecimal.valueOf(amountInCents, 2);
	}

}
