package co.com.bookhive.circulation.domain.fine.settlefine;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record SettledFineDomain(boolean newlySettled, String paymentReference) {

	public SettledFineDomain {
		paymentReference = TextHelper.getDefaultValue(paymentReference);
	}

}
