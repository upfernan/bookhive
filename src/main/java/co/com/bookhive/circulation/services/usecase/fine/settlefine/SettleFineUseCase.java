package co.com.bookhive.circulation.services.usecase.fine.settlefine;

import co.com.bookhive.circulation.domain.fine.settlefine.SettleFineDomain;
import co.com.bookhive.circulation.domain.fine.settlefine.SettledFineDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface SettleFineUseCase extends UseCaseWithReturn<SettleFineDomain, SettledFineDomain> {
}
