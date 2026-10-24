package co.com.bookhive.catalog.services.input.ports;

import java.util.List;

import co.com.bookhive.catalog.services.input.dto.response.MyReservationResponseDTO;

public interface ListMyReservationsInputPort {

	List<MyReservationResponseDTO> execute();

}
