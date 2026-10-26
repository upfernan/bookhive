package co.com.bookhive.catalog.services.input.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MyAccountResponseDTO(
		List<Loan> activeLoans,
		List<MyReservationResponseDTO> reservations,
		List<Fine> pendingFines,
		BigDecimal pendingFinesTotal,
		BigDecimal accruingFinesTotal,
		List<Return> loanHistory,
		List<Payment> payments) {

	public record Book(UUID id, String title, String authors) {
	}

	public record Loan(UUID loan, Book book, LocalDate loanDate, LocalDate dueDate, int overdueDays, BigDecimal accruedFine) {
	}

	public record Fine(UUID fine, Book book, LocalDate issueDate, int overdueDays, BigDecimal dailyRate, BigDecimal amount) {
	}

	public record Return(UUID loan, Book book, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, int overdueDays) {
	}

	public record Payment(UUID fine, Book book, String reference, BigDecimal amount, Instant paidAt) {
	}

}
