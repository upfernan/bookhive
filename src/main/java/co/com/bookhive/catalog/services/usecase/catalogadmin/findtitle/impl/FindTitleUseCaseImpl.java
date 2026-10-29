package co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl;

import java.util.UUID;
import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.TitleRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogAdminEntities;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.FindTitleUseCase;

public final class FindTitleUseCaseImpl implements FindTitleUseCase {

	private final TitleRepository titles;

	public FindTitleUseCaseImpl(final TitleRepository titles) {
		super();
		this.titles = requireDependency(titles, "el repositorio de títulos");
	}

	@Override
	public CatalogAdminDomains.Title execute(final UUID id) {
		return titleOf(titles, id);
	}

	public static CatalogAdminDomains.Title titleOf(final TitleRepository titles, final UUID id) {
		final CatalogAdminEntities.TitleEntity found = titles.findTitle(UUIDHelper.getDefaultValue(id)).orElseThrow(() ->
				BookHiveException.create(MessageKey.BOOK_TITLE_NOT_FOUND, "No existe el título " + id + "."));
		return new CatalogAdminDomains.Title(found.id(), found.title(), found.isbn(), found.year(), found.format(),
				found.status(), found.category(), found.publisher(), found.authors(), null, null);
	}

	public static CatalogAdminDomains.Category categoryOf(final CategoryRepository categories, final UUID id) {
		return categories.findCategories().stream()
				.filter(category -> category.id().equals(id))
				.map(category -> new CatalogAdminDomains.Category(category.id(), category.name(), category.status(),
						category.activeTitles()))
				.findFirst()
				.orElseThrow(() -> BookHiveException.create(MessageKey.CATEGORY_NOT_FOUND, "No existe la categoría " + id + "."));
	}

	public static CatalogAdminEntities.TitleEntity entityOf(final CatalogAdminDomains.Title title) {
		return new CatalogAdminEntities.TitleEntity(title.id(), title.title(), title.isbn(), title.year(), title.format(),
				title.status(), title.category(), title.publisher(), title.authors());
	}

	public static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED, "Un caso de uso del catálogo se creó sin " + name + ".");
		}
		return dependency;
	}

}
