package co.com.bookhive.payment.services.input.adapters;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.ConfirmFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.FinePaymentStatusDomain;
import co.com.bookhive.payment.services.input.dto.request.ConfirmFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentStatusResponseDTO;
import co.com.bookhive.payment.services.input.ports.ConfirmFinePaymentInputPort;
import co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment.ConfirmFinePaymentUseCase;

public final class ConfirmFinePaymentInputAdapter implements ConfirmFinePaymentInputPort {

	private final ConfirmFinePaymentUseCase useCase;

	public ConfirmFinePaymentInputAdapter(final ConfirmFinePaymentUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public FinePaymentStatusResponseDTO execute(final ConfirmFinePaymentRequestDTO request) {
		final ConfirmFinePaymentRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new ConfirmFinePaymentRequestDTO(null, null));
		final FinePaymentStatusDomain status = useCase.execute(
				new ConfirmFinePaymentDomain(safeRequest.reference(), safeRequest.transaction()));
		return new FinePaymentStatusResponseDTO(
				status.reference(), status.status(), status.amountInCents(), status.finePaid());
	}

}
