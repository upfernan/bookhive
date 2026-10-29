package co.com.bookhive.crosscutting.resilience;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Supplier;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class CircuitBreaker {

	public enum State {
		CLOSED,
		OPEN,
		HALF_OPEN
	}

	private final String service;
	private final int failureThreshold;
	private final Duration openDuration;
	private final Clock clock;
	private final Consumer<String> onStateChange;

	private State state = State.CLOSED;
	private int consecutiveFailures;
	private Instant openedAt = Instant.EPOCH;

	public CircuitBreaker(
			final String service,
			final int failureThreshold,
			final Duration openDuration,
			final Clock clock,
			final Consumer<String> onStateChange) {

		this.service = TextHelper.getDefaultValue(service);
		this.failureThreshold = Math.max(failureThreshold, 1);
		this.openDuration = ObjectHelper.getDefaultValue(openDuration, Duration.ofSeconds(30));
		this.clock = ObjectHelper.getDefaultValue(clock, Clock.systemUTC());
		this.onStateChange = ObjectHelper.getDefaultValue(onStateChange, _ -> { });
	}

	public <T> T call(final Supplier<T> action) {
		allowCall();
		try {
			final T result = action.get();
			recordSuccess();
			return result;
		} catch (final RuntimeException exception) {
			recordFailure();
			throw exception;
		}
	}

	public synchronized State state() {
		if (State.OPEN == state && openTimeElapsed()) {
			return State.HALF_OPEN;
		}
		return state;
	}

	private synchronized void allowCall() {
		if (State.CLOSED == state) {
			return;
		}
		if (State.OPEN == state && openTimeElapsed()) {
			change(State.HALF_OPEN);
			return;
		}
		throw new CircuitOpenException(service);
	}

	private synchronized void recordSuccess() {
		consecutiveFailures = 0;
		if (State.CLOSED != state) {
			change(State.CLOSED);
		}
	}

	private synchronized void recordFailure() {
		consecutiveFailures++;
		if (State.HALF_OPEN == state || consecutiveFailures >= failureThreshold) {
			openedAt = clock.instant();
			change(State.OPEN);
		}
	}

	private boolean openTimeElapsed() {
		return !clock.instant().isBefore(openedAt.plus(openDuration));
	}

	private void change(final State newState) {
		if (state == newState) {
			return;
		}
		state = newState;
		onStateChange.accept("El corte temporal de " + service + " pasó a " + newState
				+ " después de " + consecutiveFailures + " fallos seguidos.");
	}

}
