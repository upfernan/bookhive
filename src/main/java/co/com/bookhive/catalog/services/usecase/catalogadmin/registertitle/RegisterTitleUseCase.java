package co.com.bookhive.catalog.services.usecase.catalogadmin.registertitle;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface RegisterTitleUseCase {

	CatalogAdminDomains.Title execute(CatalogAdminDomains.Title title);

}
