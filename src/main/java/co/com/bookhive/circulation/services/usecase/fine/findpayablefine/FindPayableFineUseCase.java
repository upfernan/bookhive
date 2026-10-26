package co.com.bookhive.circulation.services.usecase.fine.findpayablefine;

import co.com.bookhive.circulation.domain.fine.findpayablefine.FindPayableFineDomain;
import co.com.bookhive.circulation.domain.fine.findpayablefine.PayableFineDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface FindPayableFineUseCase extends UseCaseWithReturn<FindPayableFineDomain, PayableFineDomain> {
}
