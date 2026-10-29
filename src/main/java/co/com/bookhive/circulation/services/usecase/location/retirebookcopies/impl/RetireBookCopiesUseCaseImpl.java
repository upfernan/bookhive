package co.com.bookhive.circulation.services.usecase.location.retirebookcopies.impl;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.usecase.location.findbookcopylocation.impl.FindBookCopyLocationUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.location.retirebookcopies.RetireBookCopiesUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class RetireBookCopiesUseCaseImpl implements RetireBookCopiesUseCase {

	private final BookCopyRepository bookCopies;
	private final ReservationRepository reservations;
	private final LocationRepository locations;

	public RetireBookCopiesUseCaseImpl(
			final BookCopyRepository bookCopies,
			final ReservationRepository reservations,
			final LocationRepository locations) {
		super();
		this.bookCopies = FindBookCopyLocationUseCaseImpl.requireDependency(bookCopies, "el repositorio de ejemplares");
		this.reservations = FindBookCopyLocationUseCaseImpl.requireDependency(reservations, "el repositorio de reservas");
		this.locations = FindBookCopyLocationUseCaseImpl.requireDependency(locations, "el repositorio de ubicaciones");
	}

	@Override
	public int execute(final UUID book) {
		if (UUIDHelper.isDefaultValue(UUIDHelper.getDefaultValue(book))) {
			throw BookHiveException.create(MessageKey.BOOK_TITLE_NOT_FOUND, "No llegó el libro cuyos ejemplares se retiran.");
		}
		final List<BookCopyEntity> copies = bookCopies.findByCriteria(BookCopyCriteria.inService(List.of(book)));
		if (copies.stream().anyMatch(copy -> !BookCopyEntity.STATUS_AVAILABLE.equals(copy.getStatus()))) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_COPIES_IN_USE,
					"El libro " + book + " tiene ejemplares prestados o apartados.");
		}
		if (!reservations.findByCriteria(ReservationCriteria.waitingForBook(book)).isEmpty()) {
			throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_RESERVATIONS_PENDING,
					"El libro " + book + " tiene reservas en espera.");
		}
		for (final BookCopyEntity copy : copies) {
			if (!bookCopies.changeStatus(copy.getId(), BookCopyEntity.STATUS_AVAILABLE, BookCopyEntity.STATUS_WITHDRAWN)) {
				throw BookHiveException.createConflict(MessageKey.BOOK_TITLE_COPIES_IN_USE,
						"El ejemplar " + copy.getBarcode() + " cambió de estado mientras se daba de baja el título.");
			}
			locations.clearValues(copy.getId());
		}
		return copies.size();
	}

}
