package co.com.bookhive.catalog.domain.book.searchbooks;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FacetValueDomain(String value, long count) {

	public FacetValueDomain {
		value = TextHelper.getDefaultValue(value);
		count = Math.max(count, 0);
	}

}
