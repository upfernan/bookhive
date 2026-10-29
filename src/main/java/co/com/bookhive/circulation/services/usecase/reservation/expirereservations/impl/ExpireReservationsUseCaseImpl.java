package co.com.bookhive.circulation.services.usecase.reservation.expirereservations.impl;

import java.time.LocalDate;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.reservation.expirereservations.ExpiredReservationsDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.usecase.reservation.expirereservations.ExpireReservationsUseCase;
import co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy.HandOverBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ExpireReservationsUseCaseImpl implements ExpireReservationsUseCase {

	private final ReservationRepository reservationRepository;
	private final BookCopyRepository bookCopyRepository;
	private final InstitutionParametersRepository institutionParametersRepository;
	private final HandOverBookCopyUseCase handOverBookCopy;

	public ExpireReservationsUseCaseImpl(
			final ReservationRepository reservationRepository,
			final BookCopyRepository bookCopyRepository,
			final InstitutionParametersRepository institutionParametersRepository,
			final HandOverBookCopyUseCase handOverBookCopy) {

		super();
		this.reservationRepository = requireDependency(reservationRepository, "el repositorio de reservas");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.handOverBookCopy = requireDependency(handOverBookCopy, "la entrega del ejemplar a la fila de reservas");
	}

	@Override
	public ExpiredReservationsDomain execute() {

		final LocalDate today = CurrentInstitution.today(CurrentInstitution.parameters(institutionParametersRepository));

		int expired = 0;
		int handedOver = 0;

		for (final ReservationEntity reservation : reservationRepository.findByCriteria(
				ReservationCriteria.assignedWithPickupBefore(today))) {

			if (!reservationRepository.changeStatus(
					reservation.getId(), ReservationEntity.STATUS_ASSIGNED, ReservationEntity.STATUS_EXPIRED)) {
				continue;
			}
			expired++;

			if (releasedCopyWentToTheQueue(reservation)) {
				handedOver++;
			}
		}

		return new ExpiredReservationsDomain(expired, handedOver);
	}

	private boolean releasedCopyWentToTheQueue(final ReservationEntity reservation) {

		if (!bookCopyRepository.changeStatus(
				reservation.getBookCopy(), BookCopyEntity.STATUS_RESERVED, BookCopyEntity.STATUS_AVAILABLE)) {
			return false;
		}

		return handOverBookCopy.execute(new HandOverBookCopyDomain(reservation.getBookCopy(), reservation.getBook()))
				.isHandedOver();
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso ExpireReservations se creó sin " + name + ".");
		}
		return dependency;
	}

}
