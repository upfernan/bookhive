package co.com.bookhive.catalog.services.input.dto.request;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record SearchBooksRequestDTO(String text, String author, String category, String publisher, int page) {

	public SearchBooksRequestDTO {
		text = TextHelper.getDefaultValue(text);
		author = TextHelper.getDefaultValue(author);
		category = TextHelper.getDefaultValue(category);
		publisher = TextHelper.getDefaultValue(publisher);
	}

}
