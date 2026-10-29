package co.com.bookhive.catalog.services.usecase.catalogadmin.registertitle.impl;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.BookCopyInventoryPort;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.TitleRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.registertitle.RegisterTitleUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;

public final class RegisterTitleUseCaseImpl implements RegisterTitleUseCase {

	private final TitleRepository titles;
	private final CategoryRepository categories;
	private final BookCopyInventoryPort inventory;
	private final CatalogSessionPort session;
	private final AuditPort audit;

	public RegisterTitleUseCaseImpl(
			final TitleRepository titles,
			final CategoryRepository categories,
			final BookCopyInventoryPort inventory,
			final CatalogSessionPort session,
			final AuditPort audit) {
		super();
		this.titles = FindTitleUseCaseImpl.requireDependency(titles, "el repositorio de títulos");
		this.categories = FindTitleUseCaseImpl.requireDependency(categories, "el repositorio de categorías");
		this.inventory = FindTitleUseCaseImpl.requireDependency(inventory, "el inventario de ejemplares");
		this.session = FindTitleUseCaseImpl.requireDependency(session, "la sesión");
		this.audit = FindTitleUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public CatalogAdminDomains.Title execute(final CatalogAdminDomains.Title input) {
		final CatalogAdminDomains.Title requested = ObjectHelper.getDefaultValue(input,
				new CatalogAdminDomains.Title(null, null, null, 0, null, null, null, null, null, null, null));
		requested.validate();
		if (!FindTitleUseCaseImpl.categoryOf(categories, requested.category()).isActive()) {
			throw BookHiveException.createConflict(MessageKey.CATEGORY_INACTIVE, "La categoría " + requested.category() + " está inactiva.");
		}
		if (titles.isbnTaken(requested.isbn(), null)) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_ISBN_TAKEN, "El ISBN " + requested.isbn() + " ya existe.");
		}
		final CatalogAdminDomains.Title title = requested.withId(UUIDHelper.generateNew(), CatalogAdminDomains.TITLE_ACTIVE);
		titles.createTitle(FindTitleUseCaseImpl.entityOf(title));
		final int copies = title.barcodes().isEmpty() ? 0 : inventory.register(title.id(), title.branch(), title.barcodes());
		audit.register(AuditPort.TITLE_REGISTERED, session.institution(), session.person(),
				title.id() + " " + title.title() + " con " + copies + " ejemplares");
		return FindTitleUseCaseImpl.titleOf(titles, title.id());
	}

}
