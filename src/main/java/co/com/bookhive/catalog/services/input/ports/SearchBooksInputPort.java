package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.request.SearchBooksRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.SearchBooksResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface SearchBooksInputPort extends InputPortWithReturn<SearchBooksRequestDTO, SearchBooksResponseDTO> {
}
