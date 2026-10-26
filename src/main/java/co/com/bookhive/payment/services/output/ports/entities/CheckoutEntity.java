package co.com.bookhive.payment.services.output.ports.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record CheckoutEntity(
		String checkoutUrl,
		String publicKey,
		String signature,
		String expirationTime,
		String redirectUrl) {

	public CheckoutEntity {
		checkoutUrl = TextHelper.getDefaultValue(checkoutUrl);
		publicKey = TextHelper.getDefaultValue(publicKey);
		signature = TextHelper.getDefaultValue(signature);
		expirationTime = TextHelper.getDefaultValue(expirationTime);
		redirectUrl = TextHelper.getDefaultValue(redirectUrl);
	}

}
