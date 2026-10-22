package co.com.bookhive.catalog.services.usecase.book.searchbooks.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import co.com.bookhive.catalog.domain.book.searchbooks.FacetValueDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBookDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.catalog.services.output.ports.BookCopiesPort;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopiesEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.BookIndexRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.FacetCountEntity;
import co.com.bookhive.catalog.services.usecase.book.searchbooks.SearchBooksUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class SearchBooksUseCaseImpl implements SearchBooksUseCase {

	private final BookIndexRepository bookIndex;
	private final BookCopiesPort bookCopies;

	public SearchBooksUseCaseImpl(final BookIndexRepository bookIndex, final BookCopiesPort bookCopies) {
		super();
		this.bookIndex = requireDependency(bookIndex, "el índice de búsqueda");
		this.bookCopies = requireDependency(bookCopies, "el conteo de ejemplares");
	}

	@Override
	public FoundBooksDomain execute(final SearchBooksDomain domain) {
		final SearchBooksDomain search = ObjectHelper.getDefaultValue(domain, new SearchBooksDomain("", "", "", "", 1));
		search.validate();

		final BookSearchPageEntity result = bookIndex.search(new BookSearchCriteria(
				search.text(),
				search.author(),
				search.category(),
				search.publisher(),
				search.page(),
				SearchBooksDomain.PAGE_SIZE));

		final Map<UUID, BookCopiesEntity> copies = copiesOf(result.books());

		return new FoundBooksDomain(
				result.found(),
				search.page(),
				result.books().stream().map(book -> toFoundBook(book, copies)).toList(),
				toFacet(result.authors()),
				toFacet(result.categories()),
				toFacet(result.publishers()));
	}

	private Map<UUID, BookCopiesEntity> copiesOf(final List<BookEntity> books) {
		if (books.isEmpty()) {
			return Map.of();
		}
		return bookCopies.countByBooks(books.stream().map(BookEntity::id).toList()).stream()
				.collect(Collectors.toMap(BookCopiesEntity::book, Function.identity(), (first, second) -> first));
	}

	private static FoundBookDomain toFoundBook(final BookEntity book, final Map<UUID, BookCopiesEntity> copies) {
		final BookCopiesEntity count = copies.getOrDefault(book.id(), new BookCopiesEntity(book.id(), 0, 0));
		return new FoundBookDomain(
				book.id(),
				book.title(),
				book.authors(),
				book.category(),
				book.publisher(),
				book.format(),
				book.year(),
				book.isbn(),
				count.available(),
				count.total());
	}

	private static List<FacetValueDomain> toFacet(final List<FacetCountEntity> counts) {
		return counts.stream().map(count -> new FacetValueDomain(count.value(), count.count())).toList();
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso SearchBooks se creó sin " + name + ".");
		}
		return dependency;
	}

}
