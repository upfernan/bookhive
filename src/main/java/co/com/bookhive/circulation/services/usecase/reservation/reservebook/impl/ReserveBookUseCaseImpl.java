package co.com.bookhive.circulation.services.usecase.reservation.reservebook.impl;

import java.time.Instant;

import co.com.bookhive.circulation.domain.reservation.ReservationQueue;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReserveBookDomain;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReservedBookDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.usecase.reservation.reservebook.ReserveBookUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ReserveBookUseCaseImpl implements ReserveBookUseCase {

	private final UserRepository userRepository;
	private final BookCopyRepository bookCopyRepository;
	private final FineRepository fineRepository;
	private final ReservationRepository reservationRepository;
	private final SessionContextPort sessionContext;
	private final AuditPort audit;

	public ReserveBookUseCaseImpl(
			final UserRepository userRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final ReservationRepository reservationRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit) {

		super();
		this.userRepository = requireDependency(userRepository, "el repositorio de usuarios");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.fineRepository = requireDependency(fineRepository, "el repositorio de multas");
		this.reservationRepository = requireDependency(reservationRepository, "el repositorio de reservas");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
	}

	@Override
	public ReservedBookDomain execute(final ReserveBookDomain domain) {

		final ReserveBookDomain reserving = requireData(domain)
				.withRepositories(userRepository, bookCopyRepository, fineRepository, reservationRepository)
				.withUser(sessionContext.getLibrarian());

		reserving.validate();

		final ReservationEntity reservation = new ReservationEntity();
		reservation.setId(UUIDHelper.generateNew());
		reservation.setBook(reserving.getBook());
		reservation.setUser(reserving.getUser());
		reservation.setStatus(ReservationEntity.STATUS_WAITING);
		reservation.setRequestedAt(Instant.now());
		reservationRepository.create(reservation);

		audit.record(
				AuditPort.RESERVATION_CREATED,
				sessionContext.getInstitution(),
				reserving.getUser(),
				reservation.getId() + " libro " + reserving.getBook());

		final int position = ReservationQueue.positionOf(
				reservation.getId(),
				reservationRepository.findByCriteria(ReservationCriteria.waitingForBook(reserving.getBook())));

		return new ReservedBookDomain(reservation.getId(), reservation.getBook(), reservation.getStatus(), position);
	}

	private static ReserveBookDomain requireData(final ReserveBookDomain domain) {
		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.create(
					MessageKey.RESERVATION_BOOK_REQUIRED,
					"El caso de uso ReserveBook recibió un dominio nulo.");
		}
		return domain;
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso ReserveBook se creó sin " + name + ".");
		}
		return dependency;
	}

}
