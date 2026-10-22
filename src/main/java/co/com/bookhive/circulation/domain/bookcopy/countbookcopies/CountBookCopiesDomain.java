package co.com.bookhive.circulation.domain.bookcopy.countbookcopies;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record CountBookCopiesDomain(List<UUID> books) {

	public static final int MAX_BOOKS = 100;

	public CountBookCopiesDomain {
		books = new ArrayList<>(ObjectHelper.getDefaultValue(books, List.<UUID>of())).stream()
				.filter(book -> !UUIDHelper.isDefaultValue(book))
				.distinct()
				.toList();
	}

	public void validate() {
		if (books.size() > MAX_BOOKS) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se pidieron los ejemplares de " + books.size() + " libros y el máximo por consulta es "
							+ MAX_BOOKS + ".");
		}
	}

}
