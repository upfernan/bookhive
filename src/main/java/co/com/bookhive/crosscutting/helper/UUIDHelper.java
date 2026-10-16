package co.com.bookhive.crosscutting.helper;

import java.util.UUID;

public final class UUIDHelper {

	private static final UUID DEFAULT_VALUE = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private UUIDHelper() {
		super();
	}

	public static UUID getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static UUID generateNew() {
		return UUID.randomUUID();
	}

	public static UUID getDefaultValue(final UUID value) {
		return ObjectHelper.getDefaultValue(value, DEFAULT_VALUE);
	}

	public static boolean isDefaultValue(final UUID value) {
		return ObjectHelper.isNull(value) || DEFAULT_VALUE.equals(value);
	}

}
