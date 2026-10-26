package co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments;

import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconcilePaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconciledPaymentsDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface ReconcilePaymentsUseCase extends UseCaseWithReturn<ReconcilePaymentsDomain, ReconciledPaymentsDomain> {
}
