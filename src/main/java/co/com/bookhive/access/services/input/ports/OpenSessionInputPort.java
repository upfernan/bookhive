package co.com.bookhive.access.services.input.ports;

import co.com.bookhive.access.services.input.dto.request.OpenSessionRequestDTO;
import co.com.bookhive.access.services.input.dto.response.OpenSessionResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface OpenSessionInputPort extends InputPortWithReturn<OpenSessionRequestDTO, OpenSessionResponseDTO> {

}
