package co.com.bookhive.circulation.domain.reservation.handoverbookcopy;

import java.util.UUID;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record HandOverBookCopyDomain(UUID bookCopy, UUID book) {

	public static final String NOTICE_KIND = "RESERVA_LISTA";

	public HandOverBookCopyDomain {
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		book = UUIDHelper.getDefaultValue(book);
	}

	public void validate() {
		if (UUIDHelper.isDefaultValue(bookCopy) || UUIDHelper.isDefaultValue(book)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"Se pidió entregar a la fila un ejemplar sin identificador o sin libro.");
		}
	}

}
