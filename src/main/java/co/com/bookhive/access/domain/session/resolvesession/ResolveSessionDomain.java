package co.com.bookhive.access.domain.session.resolvesession;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class ResolveSessionDomain {

	private final UUID person;
	private final UUID institution;

	public ResolveSessionDomain(final UUID person, final UUID institution) {
		super();
		this.person = UUIDHelper.getDefaultValue(person);
		this.institution = UUIDHelper.getDefaultValue(institution);
	}

	public UUID getPerson() {
		return person;
	}

	public UUID getInstitution() {
		return institution;
	}

}
