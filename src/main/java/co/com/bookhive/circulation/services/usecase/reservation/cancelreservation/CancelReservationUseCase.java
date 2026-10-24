package co.com.bookhive.circulation.services.usecase.reservation.cancelreservation;

import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelReservationDomain;
import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelledReservationDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface CancelReservationUseCase
		extends UseCaseWithReturn<CancelReservationDomain, CancelledReservationDomain> {
}
