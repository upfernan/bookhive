package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.RemindDueLoansRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.RemindDueLoansResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface RemindDueLoansInputPort extends InputPortWithReturn<RemindDueLoansRequestDTO, RemindDueLoansResponseDTO> {
}
