package co.com.bookhive.access.domain.permission.authorizeoperation;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class AuthorizeOperationDomain {

	private final String permission;
	private final String operation;

	public AuthorizeOperationDomain(final String permission, final String operation) {
		super();
		this.permission = TextHelper.getDefaultValue(permission).trim();
		this.operation = TextHelper.getDefaultValue(operation).trim();
	}

	public void validate() {
		if (TextHelper.isDefaultValue(permission)) {
			throw BookHiveException.createNotAllowed(
					MessageKey.PERMISSION_DENIED,
					"La operación " + operation + " no declara qué permiso exige,"
							+ " así que nadie puede usarla (ADR-027).");
		}
	}

	public String getPermission() {
		return permission;
	}

	public String getOperation() {
		return operation;
	}

}
