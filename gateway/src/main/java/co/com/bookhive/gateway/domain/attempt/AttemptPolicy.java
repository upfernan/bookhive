package co.com.bookhive.gateway.domain.attempt;

import java.time.Duration;

public record AttemptPolicy(int maxPerAccount, int maxPerOrigin, Duration window, Duration blockFor) {

	public static final int DEFAULT_MAX_PER_ACCOUNT = 5;
	public static final int DEFAULT_MAX_PER_ORIGIN = 100;
	public static final Duration DEFAULT_WINDOW = Duration.ofMinutes(10);
	public static final Duration DEFAULT_BLOCK = Duration.ofMinutes(10);

	private static final int UNAUTHORIZED = 401;
	private static final int FORBIDDEN = 403;

	public AttemptPolicy {
		maxPerAccount = maxPerAccount > 0 ? maxPerAccount : DEFAULT_MAX_PER_ACCOUNT;
		maxPerOrigin = maxPerOrigin > 0 ? Math.max(maxPerOrigin, maxPerAccount) : DEFAULT_MAX_PER_ORIGIN;
		window = positiveOrDefault(window, DEFAULT_WINDOW);
		blockFor = positiveOrDefault(blockFor, DEFAULT_BLOCK);
	}

	public static AttemptPolicy byDefault() {
		return new AttemptPolicy(DEFAULT_MAX_PER_ACCOUNT, DEFAULT_MAX_PER_ORIGIN, DEFAULT_WINDOW, DEFAULT_BLOCK);
	}

	public boolean isFailure(final int status) {
		return status == UNAUTHORIZED || status == FORBIDDEN;
	}

	public boolean accountReachedLimit(final long failures) {
		return failures >= maxPerAccount;
	}

	public boolean originReachedLimit(final long failures) {
		return failures >= maxPerOrigin;
	}

	private static Duration positiveOrDefault(final Duration value, final Duration defaultValue) {
		return value == null || value.isNegative() || value.isZero() ? defaultValue : value;
	}

}
