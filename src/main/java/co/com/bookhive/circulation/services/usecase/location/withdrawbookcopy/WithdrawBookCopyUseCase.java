package co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface WithdrawBookCopyUseCase {

	LocationDomains.BookCopyLocation execute(String barcode);

}
