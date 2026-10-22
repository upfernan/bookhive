package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.CountBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.CountBookCopiesResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface CountBookCopiesInputPort
		extends InputPortWithReturn<CountBookCopiesRequestDTO, CountBookCopiesResponseDTO> {
}
