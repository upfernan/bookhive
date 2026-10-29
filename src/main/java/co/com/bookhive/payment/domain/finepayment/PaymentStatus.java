package co.com.bookhive.payment.domain.finepayment;

import java.util.Locale;
import java.util.Set;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class PaymentStatus {

	public static final String PENDING = "PENDING";
	public static final String APPROVED = "APPROVED";
	public static final String DECLINED = "DECLINED";
	public static final String VOIDED = "VOIDED";
	public static final String ERROR = "ERROR";

	private static final Set<String> KNOWN = Set.of(PENDING, APPROVED, DECLINED, VOIDED, ERROR);
	private static final Set<String> FINAL = Set.of(APPROVED, DECLINED, VOIDED, ERROR);

	private PaymentStatus() {
		super();
	}

	public static String normalize(final String status) {
		return TextHelper.getDefaultValue(status).trim().toUpperCase(Locale.ROOT);
	}

	public static boolean isKnown(final String status) {
		return KNOWN.contains(normalize(status));
	}

	public static boolean isFinal(final String status) {
		return FINAL.contains(normalize(status));
	}

	public static boolean isApproved(final String status) {
		return APPROVED.equals(normalize(status));
	}

}
