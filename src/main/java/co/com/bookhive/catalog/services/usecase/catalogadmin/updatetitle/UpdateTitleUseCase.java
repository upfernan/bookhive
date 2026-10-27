package co.com.bookhive.catalog.services.usecase.catalogadmin.updatetitle;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface UpdateTitleUseCase {

	CatalogAdminDomains.Title execute(CatalogAdminDomains.Title title);

}
