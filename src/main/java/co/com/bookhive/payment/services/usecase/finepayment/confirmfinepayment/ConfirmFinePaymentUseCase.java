package co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment;

import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.ConfirmFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.FinePaymentStatusDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface ConfirmFinePaymentUseCase extends UseCaseWithReturn<ConfirmFinePaymentDomain, FinePaymentStatusDomain> {
}
