package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindUserInputPort
		extends InputPortWithReturn<FindUserRequestDTO, FindUserResponseDTO> {
}
