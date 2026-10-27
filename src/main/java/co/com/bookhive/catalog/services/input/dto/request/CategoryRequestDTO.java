package co.com.bookhive.catalog.services.input.dto.request;

import java.util.UUID;

public record CategoryRequestDTO(UUID id, String name) {

	public CategoryRequestDTO forId(final UUID categoryId) {
		return new CategoryRequestDTO(categoryId, name);
	}

}
