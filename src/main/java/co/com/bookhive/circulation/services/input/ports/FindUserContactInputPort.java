package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.FindUserContactRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserContactResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindUserContactInputPort extends InputPortWithReturn<FindUserContactRequestDTO, UserContactResponseDTO> {
}
