package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindBookCopyInputPort
		extends InputPortWithReturn<FindBookCopyRequestDTO, FindBookCopyResponseDTO> {
}
