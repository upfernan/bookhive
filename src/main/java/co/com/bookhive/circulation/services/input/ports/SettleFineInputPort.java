package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.SettleFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.SettleFineResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface SettleFineInputPort extends InputPortWithReturn<SettleFineRequestDTO, SettleFineResponseDTO> {
}
