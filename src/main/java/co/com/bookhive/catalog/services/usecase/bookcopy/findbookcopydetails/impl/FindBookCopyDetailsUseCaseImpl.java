package co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.impl;

import java.util.List;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.catalog.services.output.ports.BookCopyStatusPort;
import co.com.bookhive.catalog.services.output.ports.BranchDirectoryPort;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopyStatusEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.FindBookCopyDetailsUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindBookCopyDetailsUseCaseImpl implements FindBookCopyDetailsUseCase {

	private static final String AUTHOR_SEPARATOR = ", ";

	private final BookCopyStatusPort bookCopyStatus;
	private final BookRepository bookRepository;
	private final BranchDirectoryPort branches;

	public FindBookCopyDetailsUseCaseImpl(
			final BookCopyStatusPort bookCopyStatus,
			final BookRepository bookRepository,
			final BranchDirectoryPort branches) {

		super();
		this.bookCopyStatus = requireDependency(bookCopyStatus, "el estado de los ejemplares");
		this.bookRepository = requireDependency(bookRepository, "el repositorio de libros");
		this.branches = requireDependency(branches, "el directorio de sedes");
	}

	@Override
	public BookCopyDetailsDomain execute(final FindBookCopyDetailsDomain domain) {

		final FindBookCopyDetailsDomain search = ObjectHelper.getDefaultValue(domain, new FindBookCopyDetailsDomain(""));

		search.validate();

		final BookCopyStatusEntity bookCopy = bookCopyStatus.findByBarcode(search.barcode(), search.user());
		final BookEntity book = bookRepository.findByCriteria(BookCriteria.byIds(List.of(bookCopy.book()))).stream()
				.findFirst()
				.orElseGet(() -> new BookEntity(bookCopy.book(), "", "", 0, "", "", "", "", List.of()));

		return new BookCopyDetailsDomain(
				bookCopy.id(),
				bookCopy.barcode(),
				book.title(),
				String.join(AUTHOR_SEPARATOR, book.authors()),
				TextHelper.getDefaultValue(branches.nameOf(bookCopy.branch())),
				bookCopy.available(),
				bookCopy.lendable(),
				bookCopy.blocks());
	}

	private static <T> T requireDependency(final T dependency, final String name) {

		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindBookCopyDetails se creó sin " + name + ".");
		}

		return dependency;
	}

}
