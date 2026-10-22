package co.com.bookhive.shared.domain.message.resolvemessage;

import co.com.bookhive.crosscutting.helper.TextHelper;

public final class ResolveMessageDomain {

	private final String key;
	private final String text;

	public ResolveMessageDomain(final String key, final String text) {
		super();
		this.key = TextHelper.getDefaultValue(key).trim();
		this.text = TextHelper.getDefaultValue(text);
	}

	public ResolveMessageDomain withText(final String resolvedText) {
		return new ResolveMessageDomain(key, resolvedText);
	}

	public String getKey() {
		return key;
	}

	public String getText() {
		return text;
	}

}
