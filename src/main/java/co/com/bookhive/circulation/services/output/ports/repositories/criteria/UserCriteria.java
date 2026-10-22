package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.Locale;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class UserCriteria {

	private final String email;

	private UserCriteria(final String email) {
		super();
		this.email = TextHelper.getDefaultValue(email).toLowerCase(Locale.ROOT);
	}

	public static UserCriteria byEmail(final String email) {
		return new UserCriteria(email);
	}

	public String getEmail() {
		return email;
	}

	public boolean filtersByEmail() {
		return !TextHelper.isDefaultValue(email);
	}

}
