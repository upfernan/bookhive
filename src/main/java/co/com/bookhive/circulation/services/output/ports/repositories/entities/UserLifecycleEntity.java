package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

public record UserLifecycleEntity(UUID id, String status, Instant deactivatedAt) {
}
