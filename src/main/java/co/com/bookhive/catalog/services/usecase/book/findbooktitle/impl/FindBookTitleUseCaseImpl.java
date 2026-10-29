package co.com.bookhive.catalog.services.usecase.book.findbooktitle.impl;

import java.util.List;
import co.com.bookhive.catalog.domain.book.findbooktitle.FindBookTitleDomain;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.usecase.book.findbooktitle.FindBookTitleUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class FindBookTitleUseCaseImpl implements FindBookTitleUseCase {

	private final BookRepository bookRepository;

	public FindBookTitleUseCaseImpl(final BookRepository bookRepository) {
		super();
		this.bookRepository = bookRepository;
	}

	@Override
	public FindBookTitleDomain execute(final FindBookTitleDomain domain) {
		final FindBookTitleDomain request = ObjectHelper.getDefaultValue(domain, new FindBookTitleDomain(null, ""));
		if (UUIDHelper.isDefaultValue(request.book())) {
			return request;
		}
		return bookRepository.findByCriteria(BookCriteria.byIds(List.of(request.book()))).stream()
				.findFirst()
				.map(BookEntity::title)
				.map(title -> new FindBookTitleDomain(request.book(), title))
				.orElse(request);
	}

}
