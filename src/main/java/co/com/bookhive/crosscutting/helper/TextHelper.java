package co.com.bookhive.crosscutting.helper;

public final class TextHelper {

	private static final String DEFAULT_VALUE = "";

	private TextHelper() {
		super();
	}

	public static String getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static String getDefaultValue(final String value) {
		return ObjectHelper.isNull(value) ? DEFAULT_VALUE : value.trim();
	}

	public static boolean isDefaultValue(final String value) {
		return DEFAULT_VALUE.equals(getDefaultValue(value));
	}

}
