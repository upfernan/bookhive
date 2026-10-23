package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindBookCopyDetailsInputPort
		extends InputPortWithReturn<FindBookCopyDetailsRequestDTO, FindBookCopyDetailsResponseDTO> {
}
