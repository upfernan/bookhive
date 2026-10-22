package co.com.bookhive.access.services.input.dto.response;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record ResolveSchemaResponseDTO(String schema) {

	public ResolveSchemaResponseDTO {
		schema = TextHelper.getDefaultValue(schema);
	}

}
