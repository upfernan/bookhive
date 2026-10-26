package co.com.bookhive.circulation.services.input.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserAccountResponseDTO(
		List<Loan> activeLoans,
		List<Fine> pendingFines,
		BigDecimal pendingFinesTotal,
		BigDecimal accruingFinesTotal,
		List<Return> returnedLoans,
		List<Payment> payments) {

	public record Loan(UUID loan, UUID book, LocalDate loanDate, LocalDate dueDate, int overdueDays, BigDecimal accruedFine) {
	}

	public record Fine(UUID fine, UUID book, LocalDate issueDate, int overdueDays, BigDecimal dailyRate, BigDecimal amount) {
	}

	public record Return(UUID loan, UUID book, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, int overdueDays) {
	}

	public record Payment(UUID fine, UUID book, String reference, BigDecimal amount, Instant paidAt) {
	}

}
