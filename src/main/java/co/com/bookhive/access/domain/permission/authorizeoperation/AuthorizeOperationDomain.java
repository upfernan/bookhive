package co.com.bookhive.access.domain.permission.authorizeoperation;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record AuthorizeOperationDomain(String permission, String operation) {

	public AuthorizeOperationDomain {
		permission = TextHelper.getDefaultValue(permission).trim();
		operation = TextHelper.getDefaultValue(operation).trim();
	}

	public void validate() {
		if (TextHelper.isDefaultValue(permission)) {
			throw BookHiveException.createNotAllowed(
					MessageKey.PERMISSION_DENIED,
					"La operación " + operation + " no declara qué permiso exige,"
							+ " así que nadie puede usarla (ADR-027).");
		}
	}

}
