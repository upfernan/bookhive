package co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy;

import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandedOverBookCopyDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface HandOverBookCopyUseCase
		extends UseCaseWithReturn<HandOverBookCopyDomain, HandedOverBookCopyDomain> {
}
