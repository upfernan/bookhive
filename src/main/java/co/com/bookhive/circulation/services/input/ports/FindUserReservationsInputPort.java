package co.com.bookhive.circulation.services.input.ports;

import java.util.List;
import co.com.bookhive.circulation.services.input.dto.response.UserReservationResponseDTO;

public interface FindUserReservationsInputPort {

	List<UserReservationResponseDTO> execute();

}
