package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;

public interface LendBookInputPort extends InputPortWithReturn<LendBookRequestDTO, LendBookResponseDTO> {

}
