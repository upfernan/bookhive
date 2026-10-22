package co.com.bookhive.access.services.output.ports.repositories.criteria;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class PermissionCriteria {

	private final UUID person;
	private final String code;

	private PermissionCriteria(final UUID person, final String code) {
		super();
		this.person = UUIDHelper.getDefaultValue(person);
		this.code = TextHelper.getDefaultValue(code).trim();
	}

	public static PermissionCriteria grantedTo(final UUID person, final String code) {
		return new PermissionCriteria(person, code);
	}

	public UUID getPerson() {
		return person;
	}

	public String getCode() {
		return code;
	}

	public boolean filtersByPerson() {
		return !UUIDHelper.isDefaultValue(person);
	}

	public boolean filtersByCode() {
		return !TextHelper.isDefaultValue(code);
	}

}
