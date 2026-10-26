package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.RelocateBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface RelocateBookCopyInputPort extends InputPortWithReturn<RelocateBookCopyRequestDTO, LocationResponseDTOs.BookCopyLocation> {

}
