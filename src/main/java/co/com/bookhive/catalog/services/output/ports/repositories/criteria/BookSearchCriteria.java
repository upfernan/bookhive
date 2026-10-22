package co.com.bookhive.catalog.services.output.ports.repositories.criteria;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record BookSearchCriteria(String text, String author, String category, String publisher, int page, int size) {

	public BookSearchCriteria {
		text = TextHelper.getDefaultValue(text);
		author = TextHelper.getDefaultValue(author);
		category = TextHelper.getDefaultValue(category);
		publisher = TextHelper.getDefaultValue(publisher);
		page = Math.max(page, 1);
		size = Math.max(size, 0);
	}

	public static BookSearchCriteria countAll() {
		return new BookSearchCriteria("", "", "", "", 1, 0);
	}

}
