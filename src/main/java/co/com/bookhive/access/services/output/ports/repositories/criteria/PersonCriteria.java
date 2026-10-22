package co.com.bookhive.access.services.output.ports.repositories.criteria;

import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class PersonCriteria {

	private final String identity;
	private final String status;

	private PersonCriteria(final String identity, final String status) {
		super();
		this.identity = TextHelper.getDefaultValue(identity).trim();
		this.status = TextHelper.getDefaultValue(status).trim();
	}

	public static PersonCriteria activeByIdentity(final String identity) {
		return new PersonCriteria(identity, PersonEntity.STATUS_ACTIVE);
	}

	public String getIdentity() {
		return identity;
	}

	public String getStatus() {
		return status;
	}

	public boolean filtersByIdentity() {
		return !TextHelper.isDefaultValue(identity);
	}

	public boolean filtersByStatus() {
		return !TextHelper.isDefaultValue(status);
	}

}
