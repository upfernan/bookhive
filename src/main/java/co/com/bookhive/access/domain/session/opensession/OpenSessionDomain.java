package co.com.bookhive.access.domain.session.opensession;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class OpenSessionDomain {

	private final String idToken;
	private final String sessionToken;

	public OpenSessionDomain(final String idToken, final String sessionToken) {
		super();
		this.idToken = TextHelper.getDefaultValue(idToken).trim();
		this.sessionToken = TextHelper.getDefaultValue(sessionToken).trim();
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

	public String getIdToken() {
		return idToken;
	}

	public String getSessionToken() {
		return sessionToken;
	}

}
