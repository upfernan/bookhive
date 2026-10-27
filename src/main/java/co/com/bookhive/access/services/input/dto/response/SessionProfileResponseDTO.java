package co.com.bookhive.access.services.input.dto.response;

import java.util.List;

public record SessionProfileResponseDTO(String email, String institution, List<String> roles, List<String> permissions) {
}
