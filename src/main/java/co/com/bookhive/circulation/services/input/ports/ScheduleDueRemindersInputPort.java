package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.response.ScheduledTaskResponseDTO;

public interface ScheduleDueRemindersInputPort {

	ScheduledTaskResponseDTO execute();

}
