package co.com.bookhive.notification.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record SendNotificationResponseDTO(String status) {

	public SendNotificationResponseDTO {
		status = TextHelper.getDefaultValue(status);
	}

}
