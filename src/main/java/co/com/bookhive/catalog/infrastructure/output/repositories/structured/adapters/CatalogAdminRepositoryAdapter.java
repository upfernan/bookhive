package co.com.bookhive.catalog.infrastructure.output.repositories.structured.adapters;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.BookJpaRepository;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.CatalogAdminJpaRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.TitleRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogAdminEntities.CategoryEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogAdminEntities.TitleEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

@Component
public class CatalogAdminRepositoryAdapter implements CategoryRepository, TitleRepository {

	private final CatalogAdminJpaRepository repository;

	public CatalogAdminRepositoryAdapter(final CatalogAdminJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<CategoryEntity> findCategories() {
		return repository.findCategories().stream()
				.map(row -> new CategoryEntity(row.getId(), row.getName(), row.getStatus(),
						ObjectHelper.getDefaultValue(row.getTitles(), 0L)))
				.toList();
	}

	@Override
	public void createCategory(final CategoryEntity category) {
		repository.createCategory(category.id(), category.name());
	}

	@Override
	public void renameCategory(final UUID id, final String name) {
		repository.renameCategory(id, name);
	}

	@Override
	public void changeCategoryStatus(final UUID id, final String status) {
		repository.changeCategoryStatus(id, status);
	}

	@Override
	public Optional<TitleEntity> findTitle(final UUID id) {
		return repository.findTitle(id).stream().findFirst()
				.map(row -> new TitleEntity(row.getId(), row.getTitle(), TextHelper.getDefaultValue(row.getIsbn()),
						ObjectHelper.getDefaultValue(row.getYear(), 0), row.getFormat(), row.getStatus(), row.getCategory(),
						row.getPublisher(), authorsOf(row.getAuthors())));
	}

	@Override
	public boolean isbnTaken(final String isbn, final UUID except) {
		return !TextHelper.isDefaultValue(isbn) && repository.countIsbn(isbn, UUIDHelper.getDefaultValue(except)) > 0;
	}

	@Override
	public void createTitle(final TitleEntity title) {
		repository.createBook(title.id(), title.title(), title.isbn(), title.year(), title.format(), title.category(),
				publisherOf(title.publisher()));
		replaceAuthors(title);
	}

	@Override
	public void updateTitle(final TitleEntity title) {
		repository.updateBook(title.id(), title.title(), title.isbn(), title.year(), title.category(),
				publisherOf(title.publisher()));
		repository.deleteAuthors(title.id());
		replaceAuthors(title);
	}

	@Override
	public boolean withdrawTitle(final UUID id) {
		return repository.withdrawBook(id) > 0;
	}

	private void replaceAuthors(final TitleEntity title) {
		title.authors().forEach(author -> repository.addAuthor(title.id(), authorOf(author)));
	}

	private UUID publisherOf(final String name) {
		return findOrCreate(name, repository::findPublisher, repository::createPublisher);
	}

	private UUID authorOf(final String name) {
		return findOrCreate(name, repository::findAuthor, repository::createAuthor);
	}

	private static UUID findOrCreate(
			final String name, final Function<String, List<UUID>> find, final BiConsumer<UUID, String> create) {
		return find.apply(name).stream().findFirst().orElseGet(() -> {
			final UUID id = UUIDHelper.generateNew();
			create.accept(id, name);
			return id;
		});
	}

	private static List<String> authorsOf(final String authors) {
		final String safe = TextHelper.getDefaultValue(authors);
		return safe.isEmpty() ? List.of() : Arrays.asList(safe.split(BookJpaRepository.AUTHOR_SEPARATOR));
	}

}
