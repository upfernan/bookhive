package co.com.bookhive.access.services.input.dto.response;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record CurrentSessionResponseDTO(UUID institution, UUID librarian) {

	public CurrentSessionResponseDTO {
		institution = UUIDHelper.getDefaultValue(institution);
		librarian = UUIDHelper.getDefaultValue(librarian);
	}

}
