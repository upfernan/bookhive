package co.com.bookhive.catalog.services.output.ports.repositories.entities;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record CatalogChangeEntity(UUID id, UUID book) {

	public CatalogChangeEntity {
		id = UUIDHelper.getDefaultValue(id);
		book = UUIDHelper.getDefaultValue(book);
	}

}
