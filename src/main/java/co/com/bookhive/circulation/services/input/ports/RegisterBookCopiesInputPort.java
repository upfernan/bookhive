package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.RegisterBookCopiesRequestDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface RegisterBookCopiesInputPort extends InputPortWithReturn<RegisterBookCopiesRequestDTO, Integer> {

}
