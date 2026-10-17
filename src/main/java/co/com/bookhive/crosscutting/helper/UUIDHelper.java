package co.com.bookhive.crosscutting.helper;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

public final class UUIDHelper {

	private static final UUID DEFAULT_VALUE = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private static final Clock UTC_CLOCK = Clock.systemUTC();
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final long VERSION_7 = 0x7000L;
	private static final long RANDOM_A_MASK = 0x0FFFL;
	private static final long VARIANT = 0x8000000000000000L;
	private static final long RANDOM_B_MASK = 0x3FFFFFFFFFFFFFFFL;

	private UUIDHelper() {
		super();
	}

	public static UUID getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static UUID generateNew() {
		return generateNew(UTC_CLOCK);
	}

	public static UUID generateNew(final Clock clock) {
		final long timestamp = ObjectHelper.getDefaultValue(clock, UTC_CLOCK).millis();
		final long mostSignificantBits = (timestamp << 16) | VERSION_7 | (RANDOM.nextLong() & RANDOM_A_MASK);
		final long leastSignificantBits = VARIANT | (RANDOM.nextLong() & RANDOM_B_MASK);
		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	public static UUID getDefaultValue(final UUID value) {
		return ObjectHelper.getDefaultValue(value, DEFAULT_VALUE);
	}

	public static boolean isDefaultValue(final UUID value) {
		return ObjectHelper.isNull(value) || DEFAULT_VALUE.equals(value);
	}

}
