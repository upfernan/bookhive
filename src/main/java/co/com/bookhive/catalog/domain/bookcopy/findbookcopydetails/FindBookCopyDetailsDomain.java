package co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record FindBookCopyDetailsDomain(String barcode) {

	public FindBookCopyDetailsDomain {
		barcode = TextHelper.getDefaultValue(barcode);
	}

	public void validate() {

		if (TextHelper.isDefaultValue(barcode)) {
			throw BookHiveException.create(
					MessageKey.BOOK_COPY_BARCODE_REQUIRED,
					"El código del ejemplar llegó vacío en FindBookCopyDetailsDomain.");
		}
	}

}
