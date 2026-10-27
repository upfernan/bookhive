package co.com.bookhive.access.domain.session.registerperson;

import java.util.Locale;
import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record RegistrationDomain(String idToken, String name, int policyVersion) {

	private static final int MAX_NAME = 200;

	public RegistrationDomain {
		idToken = TextHelper.getDefaultValue(idToken).trim();
		name = TextHelper.getDefaultValue(name).trim();
	}

	public void validate() {
		if (TextHelper.isDefaultValue(idToken) || TextHelper.isDefaultValue(name) || name.length() > MAX_NAME) {
			throw BookHiveException.create(MessageKey.REGISTRATION_DATA_INVALID, "Registro sin pase o con un nombre inválido.");
		}
		if (policyVersion <= 0) {
			throw BookHiveException.create(MessageKey.DATA_POLICY_VERSION_REQUIRED,
					"Registro sin aceptar el aviso de tratamiento de datos.");
		}
	}

	public static String domainOf(final String email) {
		final String address = TextHelper.getDefaultValue(email).trim().toLowerCase(Locale.ROOT);
		final int at = address.lastIndexOf('@');
		return at < 1 || at == address.length() - 1 ? "" : address.substring(at + 1);
	}

	public record Registered(UUID person, String schema, String sessionToken) {
	}

}
