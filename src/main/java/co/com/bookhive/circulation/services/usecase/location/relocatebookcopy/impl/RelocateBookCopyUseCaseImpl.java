package co.com.bookhive.circulation.services.usecase.location.relocatebookcopy.impl;

import java.util.List;
import java.util.stream.IntStream;

import co.com.bookhive.circulation.domain.location.LocationDomains;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.relocatebookcopy.RelocateBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class RelocateBookCopyUseCaseImpl implements RelocateBookCopyUseCase {

	private final BookCopyRepository bookCopies;
	private final LocationRepository locations;
	private final SessionContextPort session;
	private final AuditPort audit;

	public RelocateBookCopyUseCaseImpl(
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
	public LocationDomains.BookCopyLocation execute(final LocationDomains.Relocation input) {
		final LocationDomains.Relocation relocation =
				ObjectHelper.getDefaultValue(input, new LocationDomains.Relocation(null, null, null));
		relocation.validate(locations.findLevels().size());

		final BookCopyEntity copy = FindBookCopyLocationUseCaseImpl.findBookCopy(bookCopies, relocation.barcode());
		if (BookCopyEntity.STATUS_WITHDRAWN.equals(copy.getStatus())) {
			throw BookHiveException.createConflict(MessageKey.BOOK_COPY_WITHDRAWN,
					"El ejemplar " + copy.getBarcode() + " está dado de baja.");
		}
		if (locations.findBranches().stream().noneMatch(branch -> branch.id().equals(relocation.branch()))) {
			throw BookHiveException.create(MessageKey.BRANCH_NOT_FOUND, "No existe la sede " + relocation.branch() + ".");
		}

		final String before = FindBookCopyLocationUseCaseImpl.locationOf(copy, locations).describe();
		final List<LocationEntities.ValueEntity> values = IntStream.range(0, relocation.values().size())
				.mapToObj(index -> new LocationEntities.ValueEntity(index + 1, relocation.values().get(index)))
				.toList();
		locations.relocate(copy.getId(), relocation.branch(), values);
		copy.setBranch(relocation.branch());

		final LocationDomains.BookCopyLocation after = FindBookCopyLocationUseCaseImpl.locationOf(copy, locations);
		audit.record(AuditPort.BOOK_COPY_RELOCATED, session.getInstitution(), session.getLibrarian(),
				copy.getBarcode() + ": " + before + " -> " + after.describe());
		return after;
	}

}
