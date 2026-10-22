package co.com.bookhive.catalog.services.usecase.book.searchbooks;

import co.com.bookhive.catalog.domain.book.searchbooks.FoundBooksDomain;
import co.com.bookhive.catalog.domain.book.searchbooks.SearchBooksDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface SearchBooksUseCase extends UseCaseWithReturn<SearchBooksDomain, FoundBooksDomain> {
}
