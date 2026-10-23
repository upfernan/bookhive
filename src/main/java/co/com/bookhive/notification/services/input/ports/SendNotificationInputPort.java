package co.com.bookhive.notification.services.input.ports;

import co.com.bookhive.notification.services.input.dto.request.SendNotificationRequestDTO;
import co.com.bookhive.notification.services.input.dto.response.SendNotificationResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface SendNotificationInputPort
		extends InputPortWithReturn<SendNotificationRequestDTO, SendNotificationResponseDTO> {
}
