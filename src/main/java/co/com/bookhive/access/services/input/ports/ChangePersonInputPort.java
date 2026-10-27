package co.com.bookhive.access.services.input.ports;

import co.com.bookhive.access.services.input.dto.request.ChangePersonRequestDTO;

public interface ChangePersonInputPort {

	void execute(ChangePersonRequestDTO request);

}
