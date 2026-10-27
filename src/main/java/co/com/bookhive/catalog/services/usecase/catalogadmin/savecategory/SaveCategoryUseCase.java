package co.com.bookhive.catalog.services.usecase.catalogadmin.savecategory;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface SaveCategoryUseCase {

	CatalogAdminDomains.Category execute(CatalogAdminDomains.Category category);

}
