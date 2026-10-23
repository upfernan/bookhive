package co.com.bookhive.circulation.domain.loan.returnbook;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public final class DailyRateFinePolicy implements FinePolicy {

	private static final int MONEY_SCALE = 2;

	private final BigDecimal rate;

	public DailyRateFinePolicy(final BigDecimal rate) {
		super();
		this.rate = Optional.ofNullable(rate)
				.map(BigDecimal.ZERO::max)
				.orElse(BigDecimal.ZERO)
				.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

	@Override
	public BigDecimal dailyRate() {
		return rate;
	}

	@Override
	public BigDecimal amountFor(final int overdueDays) {
		return rate.multiply(BigDecimal.valueOf(Math.max(overdueDays, 0))).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

}
