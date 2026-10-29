package co.com.bookhive.circulation.domain.reservation.reservebook;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.domain.user.BorrowerChecks;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ReserveBookDomain {

	private UUID book;
	private UUID user;

	private UserRepository userRepository;
	private BookCopyRepository bookCopyRepository;
	private FineRepository fineRepository;
	private ReservationRepository reservationRepository;

	public ReserveBookDomain(final UUID book, final UUID user) {
		super();
		setBook(book);
		setUser(user);
	}

	public ReserveBookDomain withUser(final UUID sessionUser) {
		return copyWith(sessionUser);
	}

	public ReserveBookDomain withRepositories(
			final UserRepository users,
			final BookCopyRepository bookCopies,
			final FineRepository fines,
			final ReservationRepository reservations) {

		final ReserveBookDomain withPorts = new ReserveBookDomain(book, user);
		withPorts.userRepository = users;
		withPorts.bookCopyRepository = bookCopies;
		withPorts.fineRepository = fines;
		withPorts.reservationRepository = reservations;
		return withPorts;
	}

	public void validate() {
		requireRepositories();
		validateBook();
		validateUser();

		final UserEntity found = BorrowerChecks.requireExisting(
				userRepository.findById(user), user, MessageKey.RESERVATION_USER_NOT_FOUND);
		BorrowerChecks.requireActive(found);
		BorrowerChecks.requireWithoutPendingFines(fineRepository, user);

		validateBookIsLendable();
		validateNoActiveReservation();
	}

	public List<UUID> copiesToSetAside() {
		requireRepositories();

		return lendableCopies().stream()
				.filter(copy -> BookCopyEntity.STATUS_AVAILABLE.equals(copy.getStatus()))
				.map(BookCopyEntity::getId)
				.toList();
	}

	public UUID getBook() {
		return book;
	}

	public UUID getUser() {
		return user;
	}

	private void validateBook() {
		if (UUIDHelper.isDefaultValue(book)) {
			throw BookHiveException.create(
					MessageKey.RESERVATION_BOOK_REQUIRED,
					"El identificador del libro llegó vacío en ReserveBookDomain.");
		}
	}

	private void validateUser() {
		if (UUIDHelper.isDefaultValue(user)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El usuario llegó vacío en ReserveBookDomain; debe resolverse desde la sesión.");
		}
	}

	private void validateBookIsLendable() {
		if (lendableCopies().isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.RESERVATION_BOOK_NOT_RESERVABLE,
					"El libro " + book + " no tiene ejemplares en servicio que se presten a domicilio.");
		}
	}

	private List<BookCopyEntity> lendableCopies() {
		return bookCopyRepository
				.findByCriteria(BookCopyCriteria.inService(List.of(book))).stream()
				.filter(BookCopyEntity::isLendable)
				.toList();
	}

	private void validateNoActiveReservation() {
		if (!reservationRepository.findByCriteria(ReservationCriteria.activeOfUserForBook(user, book)).isEmpty()) {
			throw BookHiveException.createConflict(
					MessageKey.RESERVATION_ALREADY_ACTIVE,
					"El usuario " + user + " ya tiene una reserva activa del libro " + book + ".");
		}
	}

	private ReserveBookDomain copyWith(final UUID newUser) {
		final ReserveBookDomain copy = new ReserveBookDomain(book, newUser);
		copy.userRepository = userRepository;
		copy.bookCopyRepository = bookCopyRepository;
		copy.fineRepository = fineRepository;
		copy.reservationRepository = reservationRepository;
		return copy;
	}

	private void requireRepositories() {
		if (ObjectHelper.isNull(userRepository)
				|| ObjectHelper.isNull(bookCopyRepository)
				|| ObjectHelper.isNull(fineRepository)
				|| ObjectHelper.isNull(reservationRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se usó ReserveBookDomain sin repositorios; falta llamar withRepositories.");
		}
	}

	private void setBook(final UUID book) {
		this.book = UUIDHelper.getDefaultValue(book);
	}

	private void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

}
