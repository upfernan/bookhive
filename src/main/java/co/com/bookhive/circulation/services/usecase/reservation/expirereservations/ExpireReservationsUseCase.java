package co.com.bookhive.circulation.services.usecase.reservation.expirereservations;

import co.com.bookhive.circulation.domain.reservation.expirereservations.ExpiredReservationsDomain;

public interface ExpireReservationsUseCase {

	ExpiredReservationsDomain execute();

}
