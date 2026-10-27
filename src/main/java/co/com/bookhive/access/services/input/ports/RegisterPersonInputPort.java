package co.com.bookhive.access.services.input.ports;

import co.com.bookhive.access.services.input.dto.request.RegisterPersonRequestDTO;
import co.com.bookhive.access.services.input.dto.response.RegisterPersonResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface RegisterPersonInputPort extends InputPortWithReturn<RegisterPersonRequestDTO, RegisterPersonResponseDTO> {

}
