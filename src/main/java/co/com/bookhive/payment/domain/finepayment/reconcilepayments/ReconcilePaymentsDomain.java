package co.com.bookhive.payment.domain.finepayment.reconcilepayments;

import java.time.Duration;
import java.time.Instant;

import co.com.bookhive.crosscutting.helper.DateHelper;

public record ReconcilePaymentsDomain(Instant now) {

	public static final Duration WAIT_BEFORE_ASKING = Duration.ofMinutes(15);
	public static final Duration LOOK_BACK = Duration.ofDays(3);

	public ReconcilePaymentsDomain {
		now = DateHelper.getDefaultValue(now);
	}

	public Instant createdFrom() {
		return now.minus(LOOK_BACK);
	}

	public Instant createdUntil() {
		return now.minus(WAIT_BEFORE_ASKING);
	}

}
