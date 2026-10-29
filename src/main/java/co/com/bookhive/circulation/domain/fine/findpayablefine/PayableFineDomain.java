package co.com.bookhive.circulation.domain.fine.findpayablefine;

import java.math.BigDecimal;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record PayableFineDomain(UUID fine, BigDecimal amount, boolean pending) {

	public PayableFineDomain {
		fine = UUIDHelper.getDefaultValue(fine);
		amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
	}

}
