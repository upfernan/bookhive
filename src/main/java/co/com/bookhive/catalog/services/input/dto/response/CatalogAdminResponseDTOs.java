package co.com.bookhive.catalog.services.input.dto.response;

import java.util.List;
import java.util.UUID;

public final class CatalogAdminResponseDTOs {

	private CatalogAdminResponseDTOs() {
		super();
	}

	public record Category(UUID id, String name, String status, long activeTitles) {
	}

	public record Title(
			UUID id, String title, String isbn, int year, String format, String status, UUID category, String publisher,
			List<String> authors) {
	}

}
