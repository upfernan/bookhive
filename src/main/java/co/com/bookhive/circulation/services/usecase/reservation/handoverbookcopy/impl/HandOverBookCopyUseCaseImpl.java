package co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy.impl;

import java.time.Instant;
import java.time.LocalDate;

import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.reservation.ReservationQueue;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandedOverBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy.HandOverBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class HandOverBookCopyUseCaseImpl implements HandOverBookCopyUseCase {

	private final ReservationRepository reservationRepository;
	private final BookCopyRepository bookCopyRepository;
	private final UserRepository userRepository;
	private final InstitutionParametersRepository institutionParametersRepository;
	private final NotificationRequestPort notificationRequests;
	private final int pickupDays;

	public HandOverBookCopyUseCaseImpl(
			final ReservationRepository reservationRepository,
			final BookCopyRepository bookCopyRepository,
			final UserRepository userRepository,
			final InstitutionParametersRepository institutionParametersRepository,
			final NotificationRequestPort notificationRequests,
			final int pickupDays) {

		super();
		this.reservationRepository = requireDependency(reservationRepository, "el repositorio de reservas");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.userRepository = requireDependency(userRepository, "el repositorio de usuarios");
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.notificationRequests = requireDependency(notificationRequests, "la cola de avisos");
		this.pickupDays = Math.max(pickupDays, 1);
	}

	@Override
	public HandedOverBookCopyDomain execute(final HandOverBookCopyDomain domain) {

		final HandOverBookCopyDomain handOver = ObjectHelper.getDefaultValue(domain, new HandOverBookCopyDomain(null, null));
		handOver.validate();

		final LocalDate pickupDeadline = CurrentInstitution
				.today(CurrentInstitution.parameters(institutionParametersRepository))
				.plusDays(pickupDays);
		final Instant assignedAt = Instant.now();

		for (final ReservationEntity next : ReservationQueue.inArrivalOrder(
				reservationRepository.findByCriteria(ReservationCriteria.waitingForBook(handOver.book())))) {

			if (reservationRepository.assign(next.getId(), handOver.bookCopy(), assignedAt, pickupDeadline)) {
				setAside(handOver);
				notificationRequests.request(new NotificationRequestEntity(
						HandOverBookCopyDomain.NOTICE_KIND,
						next.getUser(),
						handOver.book(),
						next.getId(),
						pickupDeadline));
				return new HandedOverBookCopyDomain(next.getId(), next.getUser(), nameOf(next), pickupDeadline);
			}
		}

		return HandedOverBookCopyDomain.nobody();
	}

	private void setAside(final HandOverBookCopyDomain handOver) {
		if (!bookCopyRepository.changeStatus(
				handOver.bookCopy(), BookCopyEntity.STATUS_AVAILABLE, BookCopyEntity.STATUS_RESERVED)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El ejemplar " + handOver.bookCopy() + " se asignó a una reserva pero ya no estaba "
							+ BookCopyEntity.STATUS_AVAILABLE + "; se deshace la asignación.");
		}
	}

	private String nameOf(final ReservationEntity reservation) {
		return userRepository.findById(reservation.getUser())
				.map(HandOverBookCopyUseCaseImpl::readableName)
				.filter(name -> !TextHelper.isDefaultValue(name))
				.orElse(String.valueOf(reservation.getId()));
	}

	private static String readableName(final UserEntity user) {
		return TextHelper.isDefaultValue(user.getName()) ? user.getEmail() : user.getName();
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso HandOverBookCopy se creó sin " + name + ".");
		}
		return dependency;
	}

}
