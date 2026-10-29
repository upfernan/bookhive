package co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle;

import java.util.UUID;
import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface FindTitleUseCase {

	CatalogAdminDomains.Title execute(UUID id);

}
