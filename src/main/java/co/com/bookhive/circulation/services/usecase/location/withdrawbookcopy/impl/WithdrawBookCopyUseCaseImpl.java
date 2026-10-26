package co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy.impl;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy.WithdrawBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class WithdrawBookCopyUseCaseImpl implements WithdrawBookCopyUseCase {

	private final BookCopyRepository bookCopies;
	private final LocationRepository locations;
	private final SessionContextPort session;
	private final AuditPort audit;

	public WithdrawBookCopyUseCaseImpl(
			final BookCopyRepository bookCopies,
			final LocationRepository locations,
			final SessionContextPort session,
			final AuditPort audit) {
		super();
		this.bookCopies = FindBookCopyLocationUseCaseImpl.requireDependency(bookCopies, "el repositorio de ejemplares");
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
		this.session = FindBookCopyLocationUseCaseImpl.requireDependency(session, "el contexto de la sesión");
		this.audit = FindBookCopyLocationUseCaseImpl.requireDependency(audit, "la auditoría");
	}

	@Override
	public LocationDomains.BookCopyLocation execute(final String barcode) {
		final BookCopyEntity copy = FindBookCopyLocationUseCaseImpl.findBookCopy(bookCopies, barcode);
		final String before = FindBookCopyLocationUseCaseImpl.locationOf(copy, locations).describe();
		if (!bookCopies.changeStatus(copy.getId(), BookCopyEntity.STATUS_AVAILABLE, BookCopyEntity.STATUS_WITHDRAWN)) {
			throw BookHiveException.createConflict(MessageKey.BOOK_COPY_NOT_WITHDRAWABLE,
					"El ejemplar " + copy.getBarcode() + " no está disponible: está en " + copy.getStatus() + ".");
		}
		locations.clearValues(copy.getId());
		copy.setStatus(BookCopyEntity.STATUS_WITHDRAWN);
		audit.record(AuditPort.BOOK_COPY_WITHDRAWN, session.getInstitution(), session.getLibrarian(),
				copy.getBarcode() + " estaba en " + before);
		return FindBookCopyLocationUseCaseImpl.locationOf(copy, locations);
	}

}
