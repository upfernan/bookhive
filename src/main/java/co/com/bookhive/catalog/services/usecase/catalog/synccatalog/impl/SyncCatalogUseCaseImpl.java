package co.com.bookhive.catalog.services.usecase.catalog.synccatalog.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import co.com.bookhive.catalog.domain.catalog.synccatalog.SyncCatalogDomain;
import co.com.bookhive.catalog.services.output.ports.repositories.BookIndexRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.CatalogChangeRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.CatalogChangeCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogChangeEntity;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.SyncCatalogUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

public final class SyncCatalogUseCaseImpl implements SyncCatalogUseCase {

	static final int BATCH_SIZE = 100;
	static final int MAX_BATCHES = 1_000;

	private final BookRepository bookRepository;
	private final CatalogChangeRepository catalogChangeRepository;
	private final BookIndexRepository bookIndex;

	public SyncCatalogUseCaseImpl(
			final BookRepository bookRepository,
			final CatalogChangeRepository catalogChangeRepository,
			final BookIndexRepository bookIndex) {

		super();
		this.bookRepository = requireDependency(bookRepository, "el repositorio de libros");
		this.catalogChangeRepository = requireDependency(catalogChangeRepository, "el repositorio de cambios del catálogo");
		this.bookIndex = requireDependency(bookIndex, "el índice de búsqueda");
	}

	@Override
	public SyncCatalogDomain execute() {
		final boolean fullReload = bookRepository.countActive() != bookIndex.search(BookSearchCriteria.countAll()).found();
		int indexed = fullReload ? reloadAll() : 0;
		int removed = 0;

		List<CatalogChangeEntity> pending = findPending();
		for (int batch = 0; !pending.isEmpty() && batch < MAX_BATCHES; batch++) {
			final List<UUID> books = pending.stream().map(CatalogChangeEntity::book).distinct().toList();
			final List<BookEntity> activeBooks = bookRepository.findByCriteria(BookCriteria.byIds(books)).stream()
					.filter(BookEntity::isActive)
					.toList();
			final Set<UUID> activeIds = activeBooks.stream().map(BookEntity::id).collect(Collectors.toSet());
			final List<UUID> gone = books.stream().filter(book -> !activeIds.contains(book)).toList();

			if (!activeBooks.isEmpty()) {
				bookIndex.save(activeBooks);
			}
			if (!gone.isEmpty()) {
				bookIndex.delete(gone);
			}
			catalogChangeRepository.markSent(pending.stream().map(CatalogChangeEntity::id).toList());

			indexed += activeBooks.size();
			removed += gone.size();
			pending = findPending();
		}

		return new SyncCatalogDomain(indexed, removed, fullReload);
	}

	private int reloadAll() {
		int indexed = 0;
		List<BookEntity> books = bookRepository.findByCriteria(BookCriteria.active(PageCriteria.of(0, BATCH_SIZE)));
		for (int page = 1; !books.isEmpty() && page <= MAX_BATCHES; page++) {
			bookIndex.save(books);
			indexed += books.size();
			books = books.size() < BATCH_SIZE
					? List.of()
					: bookRepository.findByCriteria(BookCriteria.active(PageCriteria.of(page, BATCH_SIZE)));
		}
		return indexed;
	}

	private List<CatalogChangeEntity> findPending() {
		return catalogChangeRepository.findByCriteria(CatalogChangeCriteria.pending(BATCH_SIZE));
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso SyncCatalog se creó sin " + name + ".");
		}
		return dependency;
	}

}
