package co.com.bookhive.circulation.services.usecase.location.withdrawbookcopy;

import java.util.List;

import co.com.bookhive.circulation.domain.location.LocationDomains;

public interface WithdrawBookCopyUseCase {

	LocationDomains.BookCopyLocation execute(String barcode);

}
