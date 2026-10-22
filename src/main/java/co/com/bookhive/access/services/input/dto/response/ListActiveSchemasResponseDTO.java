package co.com.bookhive.access.services.input.dto.response;

import java.util.List;

import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record ListActiveSchemasResponseDTO(List<String> schemas) {

	public ListActiveSchemasResponseDTO {
		schemas = List.copyOf(ObjectHelper.getDefaultValue(schemas, List.of()));
	}

}
