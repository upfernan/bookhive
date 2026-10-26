package co.com.bookhive.circulation.services.input.ports;

import java.util.List;

import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;

public interface FindLocationLevelsInputPort {

	List<LocationResponseDTOs.Level> execute();

}
