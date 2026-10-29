package co.com.bookhive.circulation.services.usecase.reservation.finduserreservations;

import java.util.List;
import co.com.bookhive.circulation.domain.reservation.finduserreservations.UserReservationDomain;

public interface FindUserReservationsUseCase {

	List<UserReservationDomain> execute();

}
