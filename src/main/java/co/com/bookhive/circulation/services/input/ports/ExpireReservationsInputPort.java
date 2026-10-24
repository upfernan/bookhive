package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.ExpireReservationsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ExpiredReservationsResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ExpireReservationsInputPort
		extends InputPortWithReturn<ExpireReservationsRequestDTO, ExpiredReservationsResponseDTO> {
}
