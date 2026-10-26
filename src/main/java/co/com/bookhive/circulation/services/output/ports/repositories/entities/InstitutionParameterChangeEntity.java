package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record InstitutionParameterChangeEntity(
		UUID id,
		String parameter,
		String previousValue,
		String newValue,
		UUID changedBy,
		Instant changedAt) {

	public InstitutionParameterChangeEntity {
		id = UUIDHelper.getDefaultValue(id);
		parameter = TextHelper.getDefaultValue(parameter).trim();
		previousValue = TextHelper.getDefaultValue(previousValue).trim();
		newValue = TextHelper.getDefaultValue(newValue).trim();
		changedBy = UUIDHelper.getDefaultValue(changedBy);
		changedAt = DateHelper.getDefaultValue(changedAt);
	}

}
