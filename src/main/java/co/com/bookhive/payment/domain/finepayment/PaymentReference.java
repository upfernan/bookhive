package co.com.bookhive.payment.domain.finepayment;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record PaymentReference(String schema, UUID payment) {

	private static final Pattern SCHEMA_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");
	private static final String SEPARATOR = "-";

	public PaymentReference {
		schema = TextHelper.getDefaultValue(schema).trim();
		payment = UUIDHelper.getDefaultValue(payment);
	}

	public static Optional<PaymentReference> parse(final String reference) {
		final String text = TextHelper.getDefaultValue(reference).trim();
		final int separator = text.indexOf(SEPARATOR);
		if (separator <= 0) {
			return Optional.empty();
		}
		final String schema = text.substring(0, separator);
		if (!SCHEMA_PATTERN.matcher(schema).matches()) {
			return Optional.empty();
		}
		try {
			final UUID payment = UUID.fromString(text.substring(separator + 1));
			return Optional.of(new PaymentReference(schema, payment)).filter(PaymentReference::isComplete);
		} catch (final IllegalArgumentException _) {
			return Optional.empty();
		}
	}

	public boolean isComplete() {
		return SCHEMA_PATTERN.matcher(schema).matches() && !UUIDHelper.isDefaultValue(payment);
	}

	public String value() {
		return schema + SEPARATOR + payment;
	}

}
