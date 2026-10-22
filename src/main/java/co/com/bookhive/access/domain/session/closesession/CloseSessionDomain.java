package co.com.bookhive.access.domain.session.closesession;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class CloseSessionDomain {

	private final String sessionToken;

	public CloseSessionDomain(final String sessionToken) {
		super();
		this.sessionToken = TextHelper.getDefaultValue(sessionToken).trim();
	}

	public boolean hasSession() {
		return !TextHelper.isDefaultValue(sessionToken);
	}

	public String getSessionToken() {
		return sessionToken;
	}

}
