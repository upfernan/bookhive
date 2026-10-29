package co.com.bookhive.circulation.services.usecase.reservation.finduserreservations.impl;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.domain.reservation.ReservationQueue;
import co.com.bookhive.circulation.domain.reservation.finduserreservations.UserReservationDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.usecase.reservation.finduserreservations.FindUserReservationsUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindUserReservationsUseCaseImpl implements FindUserReservationsUseCase {

	private final ReservationRepository reservationRepository;
	private final SessionContextPort sessionContext;

	public FindUserReservationsUseCaseImpl(
			final ReservationRepository reservationRepository,
			final SessionContextPort sessionContext) {

		super();
		this.reservationRepository = requireDependency(reservationRepository, "el repositorio de reservas");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
	}

	@Override
	public List<UserReservationDomain> execute() {

		final UUID user = sessionContext.getLibrarian();
		if (UUIDHelper.isDefaultValue(user)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"FindUserReservations no recibió el usuario de la sesión.");
		}

		return reservationRepository.findByCriteria(ReservationCriteria.activeOfUser(user)).stream()
				.map(reservation -> new UserReservationDomain(
						reservation.getId(),
						reservation.getBook(),
						reservation.getStatus(),
						positionOf(reservation),
						reservation.getRequestedAt(),
						reservation.getPickupDeadline()))
				.toList();
	}

	private int positionOf(final ReservationEntity reservation) {
		if (!reservation.isWaiting()) {
			return 0;
		}
		return ReservationQueue.positionOf(
				reservation.getId(),
				reservationRepository.findByCriteria(ReservationCriteria.waitingForBook(reservation.getBook())));
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindUserReservations se creó sin " + name + ".");
		}
		return dependency;
	}

}
