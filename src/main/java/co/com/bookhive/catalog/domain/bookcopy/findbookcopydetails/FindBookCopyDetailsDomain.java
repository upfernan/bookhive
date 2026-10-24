package co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails;

import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record FindBookCopyDetailsDomain(String barcode, UUID user) {

	public FindBookCopyDetailsDomain {
		barcode = TextHelper.getDefaultValue(barcode);
		user = UUIDHelper.getDefaultValue(user);
	}

	public FindBookCopyDetailsDomain(final String barcode) {
		this(barcode, null);
	}

	public void validate() {

		if (TextHelper.isDefaultValue(barcode)) {
			throw BookHiveException.create(
					MessageKey.BOOK_COPY_BARCODE_REQUIRED,
					"El código del ejemplar llegó vacío en FindBookCopyDetailsDomain.");
		}
	}

}
