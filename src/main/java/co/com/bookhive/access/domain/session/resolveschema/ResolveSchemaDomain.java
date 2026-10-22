package co.com.bookhive.access.domain.session.resolveschema;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class ResolveSchemaDomain {

	private final UUID institution;
	private final String schema;

	public ResolveSchemaDomain(final UUID institution, final String schema) {
		super();
		this.institution = UUIDHelper.getDefaultValue(institution);
		this.schema = TextHelper.getDefaultValue(schema).trim();
	}

	public UUID getInstitution() {
		return institution;
	}

	public String getSchema() {
		return schema;
	}

}
