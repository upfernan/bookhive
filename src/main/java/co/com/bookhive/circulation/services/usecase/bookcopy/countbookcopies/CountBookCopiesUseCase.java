package co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies;

import java.util.List;

import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.BookCopiesCountDomain;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.CountBookCopiesDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface CountBookCopiesUseCase extends UseCaseWithReturn<CountBookCopiesDomain, List<BookCopiesCountDomain>> {
}
