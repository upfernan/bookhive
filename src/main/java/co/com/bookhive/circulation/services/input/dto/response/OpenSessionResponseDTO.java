package co.com.bookhive.circulation.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class OpenSessionResponseDTO {

	private final String sessionToken;

	public OpenSessionResponseDTO(final String sessionToken) {
		super();
		this.sessionToken = TextHelper.getDefaultValue(sessionToken);
	}

	public String getSessionToken() {
		return sessionToken;
	}

}
