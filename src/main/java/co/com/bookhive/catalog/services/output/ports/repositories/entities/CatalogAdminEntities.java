package co.com.bookhive.catalog.services.output.ports.repositories.entities;

import java.util.List;
import java.util.UUID;

public final class CatalogAdminEntities {

	private CatalogAdminEntities() {
		super();
	}

	public record CategoryEntity(UUID id, String name, String status, long activeTitles) {
	}

	public record TitleEntity(
			UUID id, String title, String isbn, int year, String format, String status, UUID category, String publisher,
			List<String> authors) {
	}

}
