package co.com.bookhive.circulation.services.input.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record SettleFineRequestDTO(UUID fine, String paymentReference, BigDecimal amount) {

	public SettleFineRequestDTO {
		fine = UUIDHelper.getDefaultValue(fine);
		paymentReference = TextHelper.getDefaultValue(paymentReference);
		amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
	}

}
