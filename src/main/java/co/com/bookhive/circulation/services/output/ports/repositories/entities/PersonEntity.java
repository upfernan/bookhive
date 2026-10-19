package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class PersonEntity {

	private UUID id;
	private UUID institution;

	public PersonEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setInstitution(UUIDHelper.getDefaultValue());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public UUID getInstitution() {
		return institution;
	}

	public void setInstitution(final UUID institution) {
		this.institution = UUIDHelper.getDefaultValue(institution);
	}

}
