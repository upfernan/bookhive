package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.FindPayableFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.PayableFineResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindPayableFineInputPort extends InputPortWithReturn<FindPayableFineRequestDTO, PayableFineResponseDTO> {
}
