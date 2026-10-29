package co.com.bookhive.circulation.domain.bookcopy;

import java.util.Optional;
import java.util.UUID;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class BookCopyLendingRule {

	private BookCopyLendingRule() {
		super();
	}

	public static Optional<String> blockFor(
			final BookCopyEntity bookCopy,
			final UUID user,
			final ReservationRepository reservations) {

		final String status = bookCopy.getStatus();

		if (BookCopyEntity.STATUS_AVAILABLE.equals(status)) {
			return Optional.of(MessageKey.BOOK_COPY_NOT_RESERVED_FOR_USER);
		}

		if (BookCopyEntity.STATUS_RESERVED.equals(status)) {
			final boolean heldForUser = heldReservation(bookCopy.getId(), reservations)
					.filter(reservation -> reservation.getUser().equals(user))
					.isPresent();
			return heldForUser ? Optional.empty() : Optional.of(MessageKey.BOOK_COPY_RESERVED);
		}

		return Optional.of(MessageKey.BOOK_COPY_NOT_AVAILABLE);
	}

	public static Optional<ReservationEntity> heldReservation(
			final UUID bookCopy,
			final ReservationRepository reservations) {

		if (ObjectHelper.isNull(reservations)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El ejemplar " + bookCopy + " está " + BookCopyEntity.STATUS_RESERVED
							+ " y no se recibió el repositorio de reservas para saber a quién le corresponde.");
		}

		return reservations.findByCriteria(ReservationCriteria.assignedToBookCopy(bookCopy)).stream().findFirst();
	}

}
