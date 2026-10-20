package co.com.bookhive.gateway.domain.attempt;

public record GuardVerdict(Decision decision, long retryAfterSeconds) {

	public enum Decision {
		ADMIT,
		BLOCK,
		UNAVAILABLE
	}

	private static final GuardVerdict ADMITTED = new GuardVerdict(Decision.ADMIT, 0L);
	private static final GuardVerdict NOT_AVAILABLE = new GuardVerdict(Decision.UNAVAILABLE, 0L);

	public GuardVerdict {
		decision = decision == null ? Decision.ADMIT : decision;
		retryAfterSeconds = Math.max(0L, retryAfterSeconds);
	}

	public static GuardVerdict admit() {
		return ADMITTED;
	}

	public static GuardVerdict block(final long retryAfterSeconds) {
		return new GuardVerdict(Decision.BLOCK, Math.max(1L, retryAfterSeconds));
	}

	public static GuardVerdict unavailable() {
		return NOT_AVAILABLE;
	}

}
