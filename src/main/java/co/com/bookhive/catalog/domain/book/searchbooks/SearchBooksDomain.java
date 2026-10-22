package co.com.bookhive.catalog.domain.book.searchbooks;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record SearchBooksDomain(String text, String author, String category, String publisher, int page) {

	public static final int MAX_TEXT_LENGTH = 100;
	public static final int MAX_PAGE = 100;
	public static final int PAGE_SIZE = 20;

	public SearchBooksDomain {
		text = TextHelper.getDefaultValue(text);
		author = TextHelper.getDefaultValue(author);
		category = TextHelper.getDefaultValue(category);
		publisher = TextHelper.getDefaultValue(publisher);
	}

	public void validate() {
		validateLength("texto", text);
		validateLength("autor", author);
		validateLength("categoría", category);
		validateLength("editorial", publisher);

		if (page < 1 || page > MAX_PAGE) {
			throw BookHiveException.create(
					MessageKey.SEARCH_PAGE_INVALID,
					"Se pidió la página " + page + " de la búsqueda; el rango permitido es de 1 a " + MAX_PAGE + ".");
		}
	}

	private static void validateLength(final String field, final String value) {
		if (value.length() > MAX_TEXT_LENGTH) {
			throw BookHiveException.create(
					MessageKey.SEARCH_TEXT_TOO_LONG,
					"El campo " + field + " de la búsqueda trae " + value.length() + " caracteres y el máximo es "
							+ MAX_TEXT_LENGTH + ".");
		}
	}

}
