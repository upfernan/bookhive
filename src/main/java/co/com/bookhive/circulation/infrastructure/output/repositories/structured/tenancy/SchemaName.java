package co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy;

import java.util.regex.Pattern;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class SchemaName {

	private static final Pattern VALID_FORMAT = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private SchemaName() {
		super();
	}

	public static boolean isValid(final String schema) {
		return VALID_FORMAT.matcher(TextHelper.getDefaultValue(schema)).matches();
	}

	public static String requireValid(final String schema) {

		if (!isValid(schema)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El nombre de esquema \"" + schema + "\" no tiene el formato permitido:"
							+ " entra en una sentencia SQL y no puede ser cualquier texto.");
		}

		return schema.trim();
	}

}
