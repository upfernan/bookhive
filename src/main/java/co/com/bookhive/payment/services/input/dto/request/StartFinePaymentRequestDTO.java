package co.com.bookhive.payment.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record StartFinePaymentRequestDTO(UUID fine) {

	public StartFinePaymentRequestDTO {
		fine = UUIDHelper.getDefaultValue(fine);
	}

}
