package co.com.bookhive.access.domain.session.closesession;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record CloseSessionDomain(String sessionToken) {

	public CloseSessionDomain {
		sessionToken = TextHelper.getDefaultValue(sessionToken).trim();
	}

	public boolean hasSession() {
		return !TextHelper.isDefaultValue(sessionToken);
	}

}
