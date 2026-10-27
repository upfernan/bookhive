package co.com.bookhive.circulation.services.input.ports;

import java.util.List;

import co.com.bookhive.circulation.services.input.dto.request.SearchReadersRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FoundReaderResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface SearchReadersInputPort extends InputPortWithReturn<SearchReadersRequestDTO, List<FoundReaderResponseDTO>> {

}
