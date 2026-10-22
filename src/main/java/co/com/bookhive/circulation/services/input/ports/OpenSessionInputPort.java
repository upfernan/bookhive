package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

import co.com.bookhive.circulation.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.OpenSessionResponseDTO;

public interface OpenSessionInputPort extends InputPortWithReturn<OpenSessionRequestDTO, OpenSessionResponseDTO> {

}
