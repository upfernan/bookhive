package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.ReturnBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReturnBookResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ReturnBookInputPort extends InputPortWithReturn<ReturnBookRequestDTO, ReturnBookResponseDTO> {
}
