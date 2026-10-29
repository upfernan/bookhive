package co.com.bookhive.access.services.output.ports.repositories.criteria;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record DataPolicyAcceptanceCriteria(UUID person, int version) {

	private static final int CURRENT_VERSION = 0;

	public DataPolicyAcceptanceCriteria {
		person = UUIDHelper.getDefaultValue(person);
		version = Math.max(version, CURRENT_VERSION);
	}

	public static DataPolicyAcceptanceCriteria currentPolicyOf(final UUID person) {
		return new DataPolicyAcceptanceCriteria(person, CURRENT_VERSION);
	}

	public static DataPolicyAcceptanceCriteria of(final UUID person, final int version) {
		return new DataPolicyAcceptanceCriteria(person, version);
	}

	public boolean meansCurrentPolicy() {
		return CURRENT_VERSION == version;
	}

}
