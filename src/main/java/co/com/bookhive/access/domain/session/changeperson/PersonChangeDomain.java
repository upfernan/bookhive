package co.com.bookhive.access.domain.session.changeperson;

import java.util.UUID;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record PersonChangeDomain(UUID person, String schema, Action action) {

	public enum Action {
		DEACTIVATE,
		REACTIVATE,
		ANONYMIZE
	}

	public PersonChangeDomain {
		person = UUIDHelper.getDefaultValue(person);
		schema = TextHelper.getDefaultValue(schema).trim();
		action = action == null ? Action.DEACTIVATE : action;
	}

	public void validate() {
		if (UUIDHelper.isDefaultValue(person) || TextHelper.isDefaultValue(schema)) {
			throw BookHiveException.createUnexpected(MessageKey.REQUEST_UNEXPECTED, "Cambio de persona sin persona o sin institución.");
		}
	}

}
