package co.com.bookhive.catalog.services.usecase.catalogadmin.listcategories.impl;

import java.util.List;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.usecase.catalogadmin.listcategories.ListCategoriesUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;

public final class ListCategoriesUseCaseImpl implements ListCategoriesUseCase {

	private final CategoryRepository categories;

	public ListCategoriesUseCaseImpl(final CategoryRepository categories) {
		super();
		this.categories = FindTitleUseCaseImpl.requireDependency(categories, "el repositorio de categorías");
	}

	@Override
	public List<CatalogAdminDomains.Category> execute() {
		return categories.findCategories().stream()
				.map(category -> new CatalogAdminDomains.Category(category.id(), category.name(), category.status(),
						category.activeTitles()))
				.toList();
	}

}
