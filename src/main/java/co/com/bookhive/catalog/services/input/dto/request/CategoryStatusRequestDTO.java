package co.com.bookhive.catalog.services.input.dto.request;

import java.util.UUID;

public record CategoryStatusRequestDTO(UUID id, boolean active) {
}
