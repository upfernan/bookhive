package co.com.bookhive.catalog.services.input.dto.response;

public record SyncCatalogResponseDTO(int indexed, int removed, boolean fullReload) {
}
