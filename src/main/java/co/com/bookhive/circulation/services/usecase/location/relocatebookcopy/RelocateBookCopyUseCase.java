package co.com.bookhive.circulation.services.usecase.location.relocatebookcopy;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface RelocateBookCopyUseCase {

	LocationDomains.BookCopyLocation execute(LocationDomains.Relocation relocation);

}
