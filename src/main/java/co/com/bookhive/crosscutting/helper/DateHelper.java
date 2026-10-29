package co.com.bookhive.crosscutting.helper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class DateHelper {

	private static final LocalDate DEFAULT_VALUE = LocalDate.of(1900, 1, 1);

	private static final Instant DEFAULT_TIMESTAMP = Instant.EPOCH;

	private static final Clock UTC_CLOCK = Clock.system(ZoneOffset.UTC);

	private DateHelper() {
		super();
	}

	public static LocalDate getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static LocalDate todayInUtc() {
		return todayInUtc(UTC_CLOCK);
	}

	public static LocalDate todayInUtc(final Clock clock) {
		final Clock source = ObjectHelper.isNull(clock) ? UTC_CLOCK : clock;
		return LocalDate.ofInstant(source.instant(), ZoneOffset.UTC);
	}

	public static LocalDate todayIn(final ZoneId zone) {
		return LocalDate.now(ObjectHelper.isNull(zone) ? ZoneOffset.UTC : zone);
	}

	public static LocalDate todayIn(final ZoneId zone, final Clock clock) {
		final Instant now = ObjectHelper.isNull(clock) ? Instant.now() : clock.instant();
		return LocalDate.ofInstant(now, ObjectHelper.isNull(zone) ? ZoneOffset.UTC : zone);
	}

	public static LocalDate getDefaultValue(final LocalDate value) {
		return ObjectHelper.getDefaultValue(value, DEFAULT_VALUE);
	}

	public static boolean isDefaultValue(final LocalDate value) {
		return ObjectHelper.isNull(value) || DEFAULT_VALUE.equals(value);
	}

	public static Instant getDefaultTimestamp() {
		return DEFAULT_TIMESTAMP;
	}

	public static Instant getDefaultValue(final Instant value) {
		return ObjectHelper.getDefaultValue(value, DEFAULT_TIMESTAMP);
	}

	public static boolean isDefaultValue(final Instant value) {
		return ObjectHelper.isNull(value) || DEFAULT_TIMESTAMP.equals(value);
	}

}
