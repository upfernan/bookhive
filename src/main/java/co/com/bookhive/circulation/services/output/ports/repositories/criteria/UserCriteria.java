package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.text.Normalizer;
import java.util.Locale;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class UserCriteria {

	private final String email;
	private final String text;
	private final int limit;

	private UserCriteria(final String email, final String text, final int limit) {
		super();
		this.email = TextHelper.getDefaultValue(email).toLowerCase(Locale.ROOT);
		this.text = Normalizer.normalize(TextHelper.getDefaultValue(text).toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		this.limit = Math.max(limit, 0);
	}

	public static UserCriteria byEmail(final String email) {
		return new UserCriteria(email, null, 0);
	}

	public static UserCriteria matching(final String text, final int limit) {
		return new UserCriteria(null, text, limit);
	}

	public String getEmail() {
		return email;
	}

	public String getText() {
		return text;
	}

	public int getLimit() {
		return limit;
	}

	public boolean filtersByEmail() {
		return !TextHelper.isDefaultValue(email);
	}

	public boolean filtersByText() {
		return !TextHelper.isDefaultValue(text) && limit > 0;
	}

}
