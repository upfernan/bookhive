package co.com.bookhive.access.services.input.dto.response;

import java.util.UUID;

public record RegisterPersonResponseDTO(UUID person, String sessionToken) {
}
