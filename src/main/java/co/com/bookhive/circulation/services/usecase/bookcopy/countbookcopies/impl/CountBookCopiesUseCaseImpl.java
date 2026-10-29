package co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.impl;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.BookCopiesCountDomain;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.CountBookCopiesDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.CountBookCopiesUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class CountBookCopiesUseCaseImpl implements CountBookCopiesUseCase {

	private final BookCopyRepository bookCopyRepository;

	public CountBookCopiesUseCaseImpl(final BookCopyRepository bookCopyRepository) {
		super();
		this.bookCopyRepository = bookCopyRepository;
	}

	@Override
	public List<BookCopiesCountDomain> execute(final CountBookCopiesDomain domain) {
		final CountBookCopiesDomain request = ObjectHelper.getDefaultValue(domain, new CountBookCopiesDomain(List.of()));
		request.validate();

		if (request.books().isEmpty()) {
			return List.of();
		}

		final List<BookCopyEntity> copies = bookCopyRepository.findByCriteria(BookCopyCriteria.inService(request.books()));

		return request.books().stream()
				.map(book -> countOf(book, copies))
				.toList();
	}

	private static BookCopiesCountDomain countOf(final UUID book, final List<BookCopyEntity> copies) {
		final List<BookCopyEntity> ofBook = copies.stream()
				.filter(copy -> book.equals(copy.getBook()))
				.toList();
		final int available = (int) ofBook.stream()
				.filter(copy -> BookCopyEntity.STATUS_AVAILABLE.equals(copy.getStatus()))
				.count();
		return new BookCopiesCountDomain(book, available, ofBook.size());
	}

}
