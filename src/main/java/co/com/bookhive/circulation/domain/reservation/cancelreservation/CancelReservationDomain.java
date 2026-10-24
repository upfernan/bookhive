package co.com.bookhive.circulation.domain.reservation.cancelreservation;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.ReservationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class CancelReservationDomain {

	private UUID reservation;
	private UUID user;

	private ReservationRepository reservationRepository;

	public CancelReservationDomain(final UUID reservation, final UUID user) {
		super();
		setReservation(reservation);
		setUser(user);
	}

	public CancelReservationDomain withUser(final UUID sessionUser) {
		final CancelReservationDomain copy = new CancelReservationDomain(reservation, sessionUser);
		copy.reservationRepository = reservationRepository;
		return copy;
	}

	public CancelReservationDomain withRepositories(final ReservationRepository reservations) {
		final CancelReservationDomain withPorts = new CancelReservationDomain(reservation, user);
		withPorts.reservationRepository = reservations;
		return withPorts;
	}

	public void validate() {
		requireRepositories();

		if (UUIDHelper.isDefaultValue(reservation)) {
			throw BookHiveException.create(
					MessageKey.RESERVATION_ID_REQUIRED,
					"El identificador de la reserva llegó vacío en CancelReservationDomain.");
		}

		if (UUIDHelper.isDefaultValue(user)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El usuario llegó vacío en CancelReservationDomain; debe resolverse desde la sesión.");
		}
	}

	public ReservationEntity findCancellable() {
		requireRepositories();

		final ReservationEntity found = reservationRepository.findById(reservation)
				.filter(entity -> entity.getUser().equals(user))
				.orElseThrow(() -> BookHiveException.createConflict(
						MessageKey.RESERVATION_NOT_FOUND,
						"La reserva " + reservation + " no existe o no es del usuario " + user + "."));

		if (!found.isWaiting()) {
			throw notCancellable(found.getStatus());
		}

		return found;
	}

	public BookHiveException notCancellable(final String status) {
		return BookHiveException.createConflict(
				MessageKey.RESERVATION_NOT_CANCELLABLE,
				"La reserva " + reservation + " está en estado " + status
						+ " y solo se cancela en estado " + ReservationEntity.STATUS_WAITING + ".");
	}

	public UUID getReservation() {
		return reservation;
	}

	public UUID getUser() {
		return user;
	}

	private void requireRepositories() {
		if (ObjectHelper.isNull(reservationRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se usó CancelReservationDomain sin repositorio; falta llamar withRepositories.");
		}
	}

	private void setReservation(final UUID reservation) {
		this.reservation = UUIDHelper.getDefaultValue(reservation);
	}

	private void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

}
