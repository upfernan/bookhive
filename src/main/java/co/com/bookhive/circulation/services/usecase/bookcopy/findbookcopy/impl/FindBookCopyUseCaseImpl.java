package co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.impl;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.FindBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindBookCopyUseCaseImpl implements FindBookCopyUseCase {

	private final BookCopyRepository bookCopyRepository;

	public FindBookCopyUseCaseImpl(final BookCopyRepository bookCopyRepository) {

		super();

		if (ObjectHelper.isNull(bookCopyRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindBookCopy se creó sin el repositorio de ejemplares.");
		}

		this.bookCopyRepository = bookCopyRepository;
	}

	@Override
	public FoundBookCopyDomain execute(final FindBookCopyDomain domain) {

		final FindBookCopyDomain search = ObjectHelper.getDefaultValue(domain, new FindBookCopyDomain(""))
				.withRepositories(bookCopyRepository);

		search.validate();

		return search.find();
	}

}
