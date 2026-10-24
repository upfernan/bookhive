package co.com.bookhive.circulation.services.usecase.reservation.reservebook;

import co.com.bookhive.circulation.domain.reservation.reservebook.ReserveBookDomain;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReservedBookDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface ReserveBookUseCase extends UseCaseWithReturn<ReserveBookDomain, ReservedBookDomain> {
}
