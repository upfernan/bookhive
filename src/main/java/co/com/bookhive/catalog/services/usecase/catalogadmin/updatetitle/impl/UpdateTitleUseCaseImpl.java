package co.com.bookhive.catalog.services.usecase.catalogadmin.updatetitle.impl;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.BookCopyInventoryPort;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.TitleRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.updatetitle.UpdateTitleUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;

public final class UpdateTitleUseCaseImpl implements UpdateTitleUseCase {

	private final TitleRepository titles;
	private final CategoryRepository categories;
	private final BookCopyInventoryPort inventory;
	private final CatalogSessionPort session;
	private final AuditPort audit;

	public UpdateTitleUseCaseImpl(
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
		final CatalogAdminDomains.Title current = FindTitleUseCaseImpl.titleOf(titles, requested.id());
		requested.validate();
		if (current.isWithdrawn()) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_WITHDRAWN, "El título " + current.id() + " está de baja.");
		}
		if (!current.format().equals(requested.format())) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_FORMAT_LOCKED,
					"Se pidió cambiar el tipo de " + current.format() + " a " + requested.format() + ".");
		}
		if (!requested.category().equals(current.category())
				&& !FindTitleUseCaseImpl.categoryOf(categories, requested.category()).isActive()) {
			throw BookHiveException.createConflict(MessageKey.CATEGORY_INACTIVE, "La categoría " + requested.category() + " está inactiva.");
		}
		if (titles.isbnTaken(requested.isbn(), current.id())) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_ISBN_TAKEN, "El ISBN " + requested.isbn() + " ya existe.");
		}
		final CatalogAdminDomains.Title title = requested.withId(current.id(), current.status());
		titles.updateTitle(FindTitleUseCaseImpl.entityOf(title));
		final int copies = title.barcodes().isEmpty() ? 0 : inventory.register(title.id(), title.branch(), title.barcodes());
		audit.register(AuditPort.TITLE_UPDATED, session.institution(), session.person(),
				title.id() + " " + title.title() + (copies > 0 ? " y " + copies + " ejemplares nuevos" : ""));
		return FindTitleUseCaseImpl.titleOf(titles, title.id());
	}

}
