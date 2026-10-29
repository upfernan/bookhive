package co.com.bookhive.access.services.output.ports.repositories.entities;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class InstitutionEntity {

	public static final String STATUS_ACTIVE = "ACTIVA";

	private UUID id;
	private String schema;

	public InstitutionEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setSchema(TextHelper.getDefaultValue());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(final String schema) {
		this.schema = TextHelper.getDefaultValue(schema).trim();
	}

}
