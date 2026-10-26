package co.com.bookhive.access.domain.datapolicy.acceptdatapolicy;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

public record AcceptDataPolicyDomain(int version) {

	public void validate() {
		if (version <= 0) {
			throw BookHiveException.create(
					MessageKey.DATA_POLICY_VERSION_REQUIRED, "No llegó la versión de la política que se acepta.");
		}
	}

	public void requireCurrent(final int currentVersion) {
		if (version != currentVersion) {
			throw BookHiveException.createConflict(
					MessageKey.DATA_POLICY_VERSION_OUTDATED,
					"Se intentó aceptar la versión " + version + " pero la vigente es la " + currentVersion + ".");
		}
	}

}
