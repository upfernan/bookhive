package co.com.bookhive.circulation.domain.fine.findpayablefine;

import java.util.UUID;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record FindPayableFineDomain(UUID fine, UUID user) {

	public FindPayableFineDomain {
		fine = UUIDHelper.getDefaultValue(fine);
		user = UUIDHelper.getDefaultValue(user);
	}

	public void validate() {
		if (UUIDHelper.isDefaultValue(fine)) {
			throw BookHiveException.create(MessageKey.FINE_ID_REQUIRED, "Se pidió una multa para pagar sin su identificador.");
		}
		if (UUIDHelper.isDefaultValue(user)) {
			throw BookHiveException.createUnexpected(
					MessageKey.FINE_UNEXPECTED, "Se pidió la multa " + fine + " para pagar sin el usuario que paga.");
		}
	}

	public BookHiveException notFound() {
		return BookHiveException.create(
				MessageKey.FINE_NOT_FOUND, "La multa " + fine + " no existe o no es del usuario " + user + ".");
	}

}
