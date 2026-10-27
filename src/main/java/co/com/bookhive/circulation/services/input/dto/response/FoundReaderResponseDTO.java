package co.com.bookhive.circulation.services.input.dto.response;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FoundReaderResponseDTO(UUID id, String name, String email) {

	public FoundReaderResponseDTO {
		id = UUIDHelper.getDefaultValue(id);
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
	}

}
