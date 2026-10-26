package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinePaymentEntity(UUID fine, UUID book, String reference, BigDecimal amount, Instant paidAt) {
}
