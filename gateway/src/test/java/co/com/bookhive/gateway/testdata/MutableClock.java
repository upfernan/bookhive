package co.com.bookhive.gateway.testdata;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class MutableClock extends Clock {

	private Instant now;

	public MutableClock(final Instant start) {
		super();
		this.now = start;
	}

	public void advance(final Duration duration) {
		now = now.plus(duration);
	}

	@Override
	public ZoneId getZone() {
		return ZoneOffset.UTC;
	}

	@Override
	public Clock withZone(final ZoneId zone) {
		return this;
	}

	@Override
	public Instant instant() {
		return now;
	}

}
