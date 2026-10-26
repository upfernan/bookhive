package co.com.bookhive.circulation.services.input.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record PayableFineResponseDTO(UUID fine, BigDecimal amount, boolean pending) {

	public PayableFineResponseDTO {
		fine = UUIDHelper.getDefaultValue(fine);
		amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
	}

}
