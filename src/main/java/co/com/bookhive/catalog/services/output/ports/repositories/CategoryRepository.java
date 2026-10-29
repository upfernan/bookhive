package co.com.bookhive.catalog.services.output.ports.repositories;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogAdminEntities.CategoryEntity;

public interface CategoryRepository {

	List<CategoryEntity> findCategories();

	void createCategory(CategoryEntity category);

	void renameCategory(UUID id, String name);

	void changeCategoryStatus(UUID id, String status);

}
