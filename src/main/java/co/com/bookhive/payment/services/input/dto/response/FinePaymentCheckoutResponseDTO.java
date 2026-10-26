package co.com.bookhive.payment.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FinePaymentCheckoutResponseDTO(
		String checkoutUrl,
		String publicKey,
		String currency,
		long amountInCents,
		String reference,
		String signature,
		String expirationTime,
		String redirectUrl) {

	public FinePaymentCheckoutResponseDTO {
		checkoutUrl = TextHelper.getDefaultValue(checkoutUrl);
		publicKey = TextHelper.getDefaultValue(publicKey);
		currency = TextHelper.getDefaultValue(currency);
		reference = TextHelper.getDefaultValue(reference);
		signature = TextHelper.getDefaultValue(signature);
		expirationTime = TextHelper.getDefaultValue(expirationTime);
		redirectUrl = TextHelper.getDefaultValue(redirectUrl);
	}

}
