package co.com.bookhive.payment.services.usecase.finepayment.startfinepayment;

import co.com.bookhive.payment.domain.finepayment.startfinepayment.StartFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.FinePaymentCheckoutDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface StartFinePaymentUseCase extends UseCaseWithReturn<StartFinePaymentDomain, FinePaymentCheckoutDomain> {
}
