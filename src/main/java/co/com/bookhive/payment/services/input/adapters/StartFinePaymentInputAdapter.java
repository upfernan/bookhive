package co.com.bookhive.payment.services.input.adapters;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.FinePaymentCheckoutDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.StartFinePaymentDomain;
import co.com.bookhive.payment.services.input.dto.request.StartFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentCheckoutResponseDTO;
import co.com.bookhive.payment.services.input.ports.StartFinePaymentInputPort;
import co.com.bookhive.payment.services.usecase.finepayment.startfinepayment.StartFinePaymentUseCase;

public final class StartFinePaymentInputAdapter implements StartFinePaymentInputPort {

	private final StartFinePaymentUseCase useCase;

	public StartFinePaymentInputAdapter(final StartFinePaymentUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public FinePaymentCheckoutResponseDTO execute(final StartFinePaymentRequestDTO request) {
		final StartFinePaymentRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new StartFinePaymentRequestDTO(null));
		final FinePaymentCheckoutDomain checkout = useCase.execute(new StartFinePaymentDomain(safeRequest.fine()));
		return new FinePaymentCheckoutResponseDTO(
				checkout.checkoutUrl(),
				checkout.publicKey(),
				checkout.currency(),
				checkout.amountInCents(),
				checkout.reference(),
				checkout.signature(),
				checkout.expirationTime(),
				checkout.redirectUrl());
	}

}
