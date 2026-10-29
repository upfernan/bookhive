package co.com.bookhive.catalog.services.usecase.catalogadmin.savecategory.impl;

import java.util.UUID;
import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogAdminEntities;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.savecategory.SaveCategoryUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;

public final class SaveCategoryUseCaseImpl implements SaveCategoryUseCase {

	private final CategoryRepository categories;
	private final CatalogSessionPort session;
	private final AuditPort audit;

	public SaveCategoryUseCaseImpl(final CategoryRepository categories, final CatalogSessionPort session, final AuditPort audit) {
		super();
		this.categories = FindTitleUseCaseImpl.requireDependency(categories, "el repositorio de categorías");
		this.session = FindTitleUseCaseImpl.requireDependency(session, "la sesión");
		this.audit = FindTitleUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public CatalogAdminDomains.Category execute(final CatalogAdminDomains.Category input) {
		final CatalogAdminDomains.Category requested =
				ObjectHelper.getDefaultValue(input, new CatalogAdminDomains.Category(null, null, null, 0));
		requested.validate();
		final boolean creating = UUIDHelper.isDefaultValue(requested.id());
		if (!creating) {
			FindTitleUseCaseImpl.categoryOf(categories, requested.id());
		}
		if (categories.findCategories().stream()
				.anyMatch(other -> !other.id().equals(requested.id()) && requested.sameName(other.name()))) {
			throw BookHiveException.createConflict(MessageKey.CATEGORY_NAME_TAKEN, "Ya existe la categoría " + requested.name() + ".");
		}
		final UUID id = creating ? UUIDHelper.generateNew() : requested.id();
		if (creating) {
			categories.createCategory(new CatalogAdminEntities.CategoryEntity(id, requested.name(),
					CatalogAdminDomains.CATEGORY_ACTIVE, 0));
		} else {
			categories.renameCategory(id, requested.name());
		}
		audit.register(AuditPort.CATEGORY_SAVED, session.institution(), session.person(), id + " " + requested.name());
		return FindTitleUseCaseImpl.categoryOf(categories, id);
	}

}
