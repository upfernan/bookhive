package co.com.bookhive.crosscutting.tenancy;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class TenantContext {

	public static final String SCHEMA_WITHOUT_TENANT = "public";

	private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();

	private TenantContext() {
		super();
	}

	public static void set(final String schema) {
		CURRENT_SCHEMA.set(TextHelper.getDefaultValue(schema));
	}

	public static String get() {

		final String schema = CURRENT_SCHEMA.get();

		return TextHelper.isDefaultValue(schema) ? SCHEMA_WITHOUT_TENANT : schema;
	}

	public static void clear() {
		CURRENT_SCHEMA.remove();
	}

}
