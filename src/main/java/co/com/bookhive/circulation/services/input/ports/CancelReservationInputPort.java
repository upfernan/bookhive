package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.CancelReservationRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.CancelReservationResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface CancelReservationInputPort
		extends InputPortWithReturn<CancelReservationRequestDTO, CancelReservationResponseDTO> {
}
