package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.ReserveBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReserveBookResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ReserveBookInputPort extends InputPortWithReturn<ReserveBookRequestDTO, ReserveBookResponseDTO> {
}
