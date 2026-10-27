package co.com.bookhive.catalog.services.usecase.catalogadmin.changecategorystatus;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface ChangeCategoryStatusUseCase {

	CatalogAdminDomains.Category execute(CatalogAdminDomains.CategoryStatusChange change);

}
