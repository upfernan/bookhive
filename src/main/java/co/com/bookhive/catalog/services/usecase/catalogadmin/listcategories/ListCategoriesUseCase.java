package co.com.bookhive.catalog.services.usecase.catalogadmin.listcategories;

import java.util.List;
import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface ListCategoriesUseCase {

	List<CatalogAdminDomains.Category> execute();

}
