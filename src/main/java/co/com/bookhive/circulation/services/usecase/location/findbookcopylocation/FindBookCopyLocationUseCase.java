package co.com.bookhive.circulation.services.usecase.location.findbookcopylocation;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface FindBookCopyLocationUseCase {

	LocationDomains.BookCopyLocation execute(String barcode);

}
