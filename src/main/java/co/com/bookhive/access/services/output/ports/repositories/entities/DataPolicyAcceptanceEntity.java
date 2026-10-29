package co.com.bookhive.access.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record DataPolicyAcceptanceEntity(UUID person, int version, Instant acceptedAt) {

	public DataPolicyAcceptanceEntity {
		person = UUIDHelper.getDefaultValue(person);
		acceptedAt = DateHelper.getDefaultValue(acceptedAt);
	}

}
