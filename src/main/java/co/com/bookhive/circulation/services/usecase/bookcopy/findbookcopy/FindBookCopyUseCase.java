package co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface FindBookCopyUseCase extends UseCaseWithReturn<FindBookCopyDomain, FoundBookCopyDomain> {
}
