package co.com.bookhive.circulation.services.usecase.loan.returnbook;

import co.com.bookhive.circulation.domain.loan.returnbook.ReturnBookDomain;
import co.com.bookhive.circulation.domain.loan.returnbook.ReturnedLoanDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface ReturnBookUseCase extends UseCaseWithReturn<ReturnBookDomain, ReturnedLoanDomain> {
}
