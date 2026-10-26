package co.com.bookhive.payment.services.output.ports.entities;

import java.math.BigDecimal;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record PayableFineEntity(UUID fine, BigDecimal amount, boolean pending) {

	public PayableFineEntity {
		fine = UUIDHelper.getDefaultValue(fine);
		amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
	}

}
