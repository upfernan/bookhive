package co.com.bookhive.access.domain.session.resolveschema;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ResolveSchemaDomain(UUID institution, String schema) {

	public ResolveSchemaDomain {
		institution = UUIDHelper.getDefaultValue(institution);
		schema = TextHelper.getDefaultValue(schema).trim();
	}

}
