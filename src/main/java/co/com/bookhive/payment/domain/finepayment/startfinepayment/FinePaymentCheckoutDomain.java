package co.com.bookhive.payment.domain.finepayment.startfinepayment;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FinePaymentCheckoutDomain(
		String checkoutUrl,
		String publicKey,
		String currency,
		long amountInCents,
		String reference,
		String signature,
		String expirationTime,
		String redirectUrl) {

	public FinePaymentCheckoutDomain {
		checkoutUrl = TextHelper.getDefaultValue(checkoutUrl);
		publicKey = TextHelper.getDefaultValue(publicKey);
		currency = TextHelper.getDefaultValue(currency);
		amountInCents = Math.max(amountInCents, 0L);
		reference = TextHelper.getDefaultValue(reference);
		signature = TextHelper.getDefaultValue(signature);
		expirationTime = TextHelper.getDefaultValue(expirationTime);
		redirectUrl = TextHelper.getDefaultValue(redirectUrl);
	}

}
