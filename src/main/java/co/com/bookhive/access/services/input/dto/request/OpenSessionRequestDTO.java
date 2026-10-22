package co.com.bookhive.access.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class OpenSessionRequestDTO {

	private String idToken;

	public OpenSessionRequestDTO() {
		super();
		setIdToken(TextHelper.getDefaultValue());
	}

	public OpenSessionRequestDTO(final String idToken) {
		super();
		setIdToken(idToken);
	}

	public String getIdToken() {
		return idToken;
	}

	private void setIdToken(final String idToken) {
		this.idToken = TextHelper.getDefaultValue(idToken);
	}

}
