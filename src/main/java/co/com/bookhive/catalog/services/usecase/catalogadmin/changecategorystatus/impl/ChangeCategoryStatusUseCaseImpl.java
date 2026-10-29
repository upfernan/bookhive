package co.com.bookhive.catalog.services.usecase.catalogadmin.changecategorystatus.impl;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.changecategorystatus.ChangeCategoryStatusUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;

public final class ChangeCategoryStatusUseCaseImpl implements ChangeCategoryStatusUseCase {

	private final CategoryRepository categories;
	private final CatalogSessionPort session;
	private final AuditPort audit;

	public ChangeCategoryStatusUseCaseImpl(
			final CategoryRepository categories, final CatalogSessionPort session, final AuditPort audit) {
		super();
		this.categories = FindTitleUseCaseImpl.requireDependency(categories, "el repositorio de categorías");
		this.session = FindTitleUseCaseImpl.requireDependency(session, "la sesión");
		this.audit = FindTitleUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public CatalogAdminDomains.Category execute(final CatalogAdminDomains.CategoryStatusChange input) {
		final CatalogAdminDomains.CategoryStatusChange change =
				ObjectHelper.getDefaultValue(input, new CatalogAdminDomains.CategoryStatusChange(null, true));
		final CatalogAdminDomains.Category category = FindTitleUseCaseImpl.categoryOf(categories, change.id());
		if (!change.active() && category.activeTitles() > 0) {
			throw BookHiveException.createConflict(MessageKey.CATEGORY_IN_USE,
					"La categoría " + category.name() + " tiene " + category.activeTitles() + " títulos activos.");
		}
		final String status = change.active() ? CatalogAdminDomains.CATEGORY_ACTIVE : CatalogAdminDomains.CATEGORY_INACTIVE;
		categories.changeCategoryStatus(category.id(), status);
		audit.register(AuditPort.CATEGORY_STATUS_CHANGED, session.institution(), session.person(), category.name() + " " + status);
		return FindTitleUseCaseImpl.categoryOf(categories, category.id());
	}

}
