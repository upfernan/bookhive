package co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface FindBookCopyDetailsUseCase
		extends UseCaseWithReturn<FindBookCopyDetailsDomain, BookCopyDetailsDomain> {
}
