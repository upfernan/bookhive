package co.com.bookhive.payment.services.input.adapters;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.PaymentEventDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.RecordedPaymentEventDomain;
import co.com.bookhive.payment.services.input.dto.request.PaymentEventRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.PaymentEventResponseDTO;
import co.com.bookhive.payment.services.input.ports.RecordPaymentEventInputPort;
import co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent.RecordPaymentEventUseCase;

public final class RecordPaymentEventInputAdapter implements RecordPaymentEventInputPort {

	private final RecordPaymentEventUseCase useCase;

	public RecordPaymentEventInputAdapter(final RecordPaymentEventUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public PaymentEventResponseDTO execute(final PaymentEventRequestDTO request) {
		final PaymentEventRequestDTO event = ObjectHelper.getDefaultValue(
				request, new PaymentEventRequestDTO(null, null, null, 0L, null, 0L, null));
		final RecordedPaymentEventDomain recorded = useCase.execute(new PaymentEventDomain(
				event.reference(),
				event.transaction(),
				event.status(),
				event.amountInCents(),
				event.signedValues(),
				event.timestamp(),
				event.checksum()));
		return new PaymentEventResponseDTO(recorded.applied());
	}

}
