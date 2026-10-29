package co.com.bookhive.circulation.services.usecase.reservation.cancelreservation.impl;

import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelReservationDomain;
import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelledReservationDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.usecase.reservation.cancelreservation.CancelReservationUseCase;
import co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy.HandOverBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class CancelReservationUseCaseImpl implements CancelReservationUseCase {

	private final ReservationRepository reservationRepository;
	private final BookCopyRepository bookCopyRepository;
	private final SessionContextPort sessionContext;
	private final AuditPort audit;
	private final HandOverBookCopyUseCase handOverBookCopy;

	public CancelReservationUseCaseImpl(
			final ReservationRepository reservationRepository,
			final BookCopyRepository bookCopyRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit,
			final HandOverBookCopyUseCase handOverBookCopy) {

		super();
		this.reservationRepository = requireDependency(reservationRepository, "el repositorio de reservas");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
		this.handOverBookCopy = requireDependency(handOverBookCopy, "la entrega del ejemplar a la fila de reservas");
	}

	@Override
	public CancelledReservationDomain execute(final CancelReservationDomain domain) {

		final CancelReservationDomain cancelling = requireData(domain)
				.withRepositories(reservationRepository)
				.withUser(sessionContext.getLibrarian());

		cancelling.validate();

		final ReservationEntity reservation = cancelling.findCancellable();
		final boolean hadBookCopySetAside = reservation.isAssigned();

		if (!reservationRepository.changeStatus(
				reservation.getId(), reservation.getStatus(), ReservationEntity.STATUS_CANCELLED)) {
			throw cancelling.notCancellable(currentStatusOf(reservation));
		}

		if (hadBookCopySetAside) {
			releaseBookCopy(reservation);
		}

		audit.register(
				AuditPort.RESERVATION_CANCELLED,
				sessionContext.getInstitution(),
				cancelling.getUser(),
				String.valueOf(reservation.getId()));

		return new CancelledReservationDomain(
				reservation.getId(), reservation.getBook(), ReservationEntity.STATUS_CANCELLED);
	}

	private void releaseBookCopy(final ReservationEntity reservation) {
		if (bookCopyRepository.changeStatus(
				reservation.getBookCopy(), BookCopyEntity.STATUS_RESERVED, BookCopyEntity.STATUS_AVAILABLE)) {
			handOverBookCopy.execute(new HandOverBookCopyDomain(reservation.getBookCopy(), reservation.getBook()));
		}
	}

	private String currentStatusOf(final ReservationEntity reservation) {
		return reservationRepository.findById(reservation.getId())
				.map(ReservationEntity::getStatus)
				.orElse(reservation.getStatus());
	}

	private static CancelReservationDomain requireData(final CancelReservationDomain domain) {
		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.create(
					MessageKey.RESERVATION_ID_REQUIRED,
					"El caso de uso CancelReservation recibió un dominio nulo.");
		}
		return domain;
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso CancelReservation se creó sin " + name + ".");
		}
		return dependency;
	}

}
