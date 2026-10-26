package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.LocalDate;
import java.util.UUID;

public record ReturnedLoanEntity(UUID loan, UUID book, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, int overdueDays) {
}
