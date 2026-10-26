package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.BranchRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface CreateBranchInputPort extends InputPortWithReturn<BranchRequestDTO, LocationResponseDTOs.Branch> {

}
