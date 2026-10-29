package co.com.bookhive.catalog.services.usecase.catalogadmin.withdrawtitle;

import java.util.UUID;
import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;

public interface WithdrawTitleUseCase {

	CatalogAdminDomains.Title execute(UUID id);

}
