package co.com.bookhive.circulation.services.input.ports;

import java.util.List;
import co.com.bookhive.circulation.services.input.dto.request.LocationLevelsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ConfigureLocationLevelsInputPort extends InputPortWithReturn<LocationLevelsRequestDTO, List<LocationResponseDTOs.Level>> {

}
