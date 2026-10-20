package co.com.bookhive.gateway.domain.attempt;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;

public record AttemptRequest(String action, String account, String origin, boolean administrative) {

	static final String UNKNOWN_ORIGIN = "desconocido";

	public AttemptRequest {
		action = TextHelper.getDefaultValue(action);
		account = TextHelper.getDefaultValue(account);
		origin = TextHelper.getDefaultValue(origin, UNKNOWN_ORIGIN);
	}

	public boolean isSensitive() {
		return !TextHelper.isDefaultValue(action);
	}

	public boolean hasAccount() {
		return !TextHelper.isDefaultValue(account);
	}

	public boolean needsGuard() {
		return isSensitive() || administrative;
	}

}
