package co.com.bookhive.initializer.migrations;

import java.util.Map;
import java.util.regex.Pattern;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class MigrationPlaceholders {

	static final String APPLICATION_USER = "appUser";
	static final String DEFAULT_APPLICATION_USER = "bookhive_app";

	private static final Pattern ROLE_NAME = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private MigrationPlaceholders() {
		super();
	}

	public static Map<String, String> forApplicationUser(final String applicationUser) {

		final String name = TextHelper.isDefaultValue(applicationUser)
				? DEFAULT_APPLICATION_USER
				: TextHelper.getDefaultValue(applicationUser);

		if (!ROLE_NAME.matcher(name).matches()) {
			throw new IllegalArgumentException(
					"El usuario de la aplicación \"" + name + "\" no tiene el formato permitido:"
							+ " entra en las sentencias de permisos y no puede ser cualquier texto.");
		}

		return Map.of(APPLICATION_USER, name);
	}

}
