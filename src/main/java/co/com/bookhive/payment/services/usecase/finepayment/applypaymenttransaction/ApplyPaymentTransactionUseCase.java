package co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction;

import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface ApplyPaymentTransactionUseCase extends UseCaseWithReturn<PaymentTransactionDomain, AppliedPaymentTransactionDomain> {
}
