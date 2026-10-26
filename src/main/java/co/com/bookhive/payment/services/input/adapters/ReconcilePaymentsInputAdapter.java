package co.com.bookhive.payment.services.input.adapters;

import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconcilePaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconciledPaymentsDomain;
import co.com.bookhive.payment.services.input.dto.response.ReconcilePaymentsResponseDTO;
import co.com.bookhive.payment.services.input.ports.ReconcilePaymentsInputPort;
import co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments.ReconcilePaymentsUseCase;

public final class ReconcilePaymentsInputAdapter implements ReconcilePaymentsInputPort {

	private final ReconcilePaymentsUseCase useCase;

	public ReconcilePaymentsInputAdapter(final ReconcilePaymentsUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ReconcilePaymentsResponseDTO execute() {
		final ReconciledPaymentsDomain reconciled = useCase.execute(new ReconcilePaymentsDomain(null));
		return new ReconcilePaymentsResponseDTO(reconciled.institutions(), reconciled.reviewed(), reconciled.finesPaid());
	}

}
