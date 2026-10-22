package co.com.bookhive.access.domain.session.opensession;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record OpenSessionDomain(String idToken, String sessionToken) {

	public OpenSessionDomain {
		idToken = TextHelper.getDefaultValue(idToken).trim();
		sessionToken = TextHelper.getDefaultValue(sessionToken).trim();
	}

	public void validate() {
		if (TextHelper.isDefaultValue(idToken)) {
			throw BookHiveException.create(
					MessageKey.SESSION_ID_TOKEN_REQUIRED,
					"Se pidió abrir una sesión sin el pase que entrega el proveedor de identidad.");
		}
	}

	public OpenSessionDomain withSessionToken(final String openedSessionToken) {
		return new OpenSessionDomain(idToken, openedSessionToken);
	}

}
