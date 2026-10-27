package co.com.bookhive.circulation.services.usecase.user.searchreaders;

import java.util.List;

import co.com.bookhive.circulation.domain.user.searchreaders.FoundReaderDomain;
import co.com.bookhive.circulation.domain.user.searchreaders.SearchReadersDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface SearchReadersUseCase extends UseCaseWithReturn<SearchReadersDomain, List<FoundReaderDomain>> {

}
