package co.com.bookhive.circulation.services.usecase.location.registerbookcopies;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface RegisterBookCopiesUseCase {

	int execute(LocationDomains.NewCopies copies);

}
