package co.com.bookhive.crosscutting.helper;

public final class ObjectHelper {

	private ObjectHelper() {
		super();
	}

	public static <T> T getDefaultValue(final T value, final T defaultValue) {
		return isNull(value) ? defaultValue : value;
	}

	public static boolean isNull(final Object value) {
		return value == null;
	}

}
