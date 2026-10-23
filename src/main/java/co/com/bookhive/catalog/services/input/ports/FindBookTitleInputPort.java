package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.request.FindBookTitleRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.BookTitleResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindBookTitleInputPort extends InputPortWithReturn<FindBookTitleRequestDTO, BookTitleResponseDTO> {
}
