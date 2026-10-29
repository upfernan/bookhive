package co.com.bookhive.catalog.testdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import co.com.bookhive.catalog.services.output.ports.BookCopiesPort;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopiesEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.BookIndexRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.CatalogChangeRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.CatalogChangeCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogChangeEntity;

public final class FakeCatalog {

	private FakeCatalog() {
		super();
	}

	public static BookEntity book(final String title, final String status) {
		return new BookEntity(UUID.randomUUID(), title, "978-0134494166", 2017, "FISICO", status,
				"Ingeniería de software", "Pearson", List.of("Robert C. Martin"));
	}

	public static final class Books implements BookRepository {

		private final List<BookEntity> stored = new ArrayList<>();
		private final List<BookCriteria> searches = new ArrayList<>();

		public Books with(final BookEntity book) {
			stored.add(book);
			return this;
		}

		public List<BookCriteria> searches() {
			return searches;
		}

		@Override
		public List<BookEntity> findByCriteria(final BookCriteria criteria) {
			searches.add(criteria);
			if (criteria.filtersByIds()) {
				return stored.stream().filter(book -> criteria.getIds().contains(book.id())).toList();
			}
			final List<BookEntity> active = stored.stream().filter(BookEntity::isActive).toList();
			final int from = Math.min(criteria.getPage().getPage() * criteria.getPage().getSize(), active.size());
			final int to = Math.min(from + criteria.getPage().getSize(), active.size());
			return active.subList(from, to);
		}

		@Override
		public long countActive() {
			return stored.stream().filter(BookEntity::isActive).count();
		}

	}

	public static final class Changes implements CatalogChangeRepository {

		private final List<CatalogChangeEntity> pending = new ArrayList<>();
		private final List<UUID> sent = new ArrayList<>();

		public Changes with(final UUID book) {
			pending.add(new CatalogChangeEntity(UUID.randomUUID(), book));
			return this;
		}

		public List<UUID> sent() {
			return sent;
		}

		@Override
		public List<CatalogChangeEntity> findByCriteria(final CatalogChangeCriteria criteria) {
			return pending.stream().filter(change -> !sent.contains(change.id())).limit(criteria.limit()).toList();
		}

		@Override
		public void markSent(final List<UUID> changes) {
			sent.addAll(changes);
		}

	}

	public static final class Index implements BookIndexRepository {

		private final List<BookEntity> saved = new ArrayList<>();
		private final List<UUID> deleted = new ArrayList<>();
		private final List<BookSearchCriteria> searches = new ArrayList<>();
		private BookSearchPageEntity result = new BookSearchPageEntity(0, List.of(), List.of(), List.of(), List.of());
		private long documents;

		public Index answering(final BookSearchPageEntity page) {
			result = page;
			return this;
		}

		public Index holding(final long count) {
			documents = count;
			return this;
		}

		public List<BookEntity> saved() {
			return saved;
		}

		public List<UUID> deleted() {
			return deleted;
		}

		public List<BookSearchCriteria> searches() {
			return searches;
		}

		@Override
		public BookSearchPageEntity search(final BookSearchCriteria criteria) {
			searches.add(criteria);
			if (criteria.equals(BookSearchCriteria.countAll())) {
				return new BookSearchPageEntity(documents, List.of(), List.of(), List.of(), List.of());
			}
			return result;
		}

		@Override
		public void save(final List<BookEntity> books) {
			saved.addAll(books);
		}

		@Override
		public void delete(final List<UUID> books) {
			deleted.addAll(books);
		}

	}

	public static final class Copies implements BookCopiesPort {

		private final List<BookCopiesEntity> counts = new ArrayList<>();
		private final List<List<UUID>> requests = new ArrayList<>();

		public Copies with(final UUID book, final int available, final int total) {
			counts.add(new BookCopiesEntity(book, available, total));
			return this;
		}

		public List<List<UUID>> requests() {
			return requests;
		}

		@Override
		public List<BookCopiesEntity> countByBooks(final List<UUID> books) {
			requests.add(books);
			return List.copyOf(counts);
		}

	}

}
