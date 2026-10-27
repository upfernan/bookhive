package co.com.bookhive.catalog.services.usecase.catalogadmin.withdrawtitle.impl;

import java.util.UUID;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.output.ports.BookCopyInventoryPort;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.output.ports.repositories.TitleRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.withdrawtitle.WithdrawTitleUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;

public final class WithdrawTitleUseCaseImpl implements WithdrawTitleUseCase {

	private final TitleRepository titles;
	private final BookCopyInventoryPort inventory;
	private final CatalogSessionPort session;
	private final AuditPort audit;

	public WithdrawTitleUseCaseImpl(
			final TitleRepository titles,
			final BookCopyInventoryPort inventory,
			final CatalogSessionPort session,
			final AuditPort audit) {
		super();
		this.titles = FindTitleUseCaseImpl.requireDependency(titles, "el repositorio de títulos");
		this.inventory = FindTitleUseCaseImpl.requireDependency(inventory, "el inventario de ejemplares");
		this.session = FindTitleUseCaseImpl.requireDependency(session, "la sesión");
		this.audit = FindTitleUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public CatalogAdminDomains.Title execute(final UUID id) {
		final CatalogAdminDomains.Title current = FindTitleUseCaseImpl.titleOf(titles, id);
		if (current.isWithdrawn()) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_WITHDRAWN, "El título " + id + " ya está de baja.");
		}
		final int copies = inventory.retireAll(current.id());
		if (!titles.withdrawTitle(current.id())) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_WITHDRAWN, "El título " + id + " cambió mientras se daba de baja.");
		}
		audit.record(AuditPort.TITLE_WITHDRAWN, session.institution(), session.person(),
				current.id() + " " + current.title() + " y " + copies + " ejemplares");
		return FindTitleUseCaseImpl.titleOf(titles, current.id());
	}

}
