package co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent;

import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.PaymentEventDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.RecordedPaymentEventDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface RecordPaymentEventUseCase extends UseCaseWithReturn<PaymentEventDomain, RecordedPaymentEventDomain> {
}
