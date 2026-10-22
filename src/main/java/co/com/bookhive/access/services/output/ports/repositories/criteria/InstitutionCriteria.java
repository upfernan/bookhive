package co.com.bookhive.access.services.output.ports.repositories.criteria;

import java.util.UUID;

import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class InstitutionCriteria {

	private final UUID id;
	private final String status;

	private InstitutionCriteria(final UUID id, final String status) {
		super();
		this.id = UUIDHelper.getDefaultValue(id);
		this.status = TextHelper.getDefaultValue(status).trim();
	}

	public static InstitutionCriteria activeById(final UUID id) {
		return new InstitutionCriteria(id, InstitutionEntity.STATUS_ACTIVE);
	}

	public UUID getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public boolean filtersById() {
		return !UUIDHelper.isDefaultValue(id);
	}

	public boolean filtersByStatus() {
		return !TextHelper.isDefaultValue(status);
	}

}
