package co.com.bookhive.shared.domain.message.resolvemessage;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record ResolveMessageDomain(String key, String text) {

	public ResolveMessageDomain {
		key = TextHelper.getDefaultValue(key);
		text = TextHelper.getDefaultValue(text);
	}

	public ResolveMessageDomain withText(final String resolvedText) {
		return new ResolveMessageDomain(key, resolvedText);
	}

}
