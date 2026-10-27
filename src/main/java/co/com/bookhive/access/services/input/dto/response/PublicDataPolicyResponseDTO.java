package co.com.bookhive.access.services.input.dto.response;

import java.time.Instant;

public record PublicDataPolicyResponseDTO(int version, String content, Instant publishedAt) {
}
