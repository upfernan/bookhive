package co.com.bookhive.circulation.domain.account.finduseraccount;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import co.com.bookhive.circulation.domain.loan.returnbook.FinePolicy;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record UserAccountDomain(
		List<ActiveLoan> activeLoans,
		List<PendingFine> pendingFines,
		List<ReturnedLoan> returnedLoans,
		List<Payment> payments) {

	public record ActiveLoan(UUID loan, UUID book, LocalDate loanDate, LocalDate dueDate, int overdueDays, BigDecimal accruedFine) {

		public static ActiveLoan on(
				final UUID loan, final UUID book, final LocalDate loanDate, final LocalDate dueDate,
				final LocalDate today, final FinePolicy policy) {

			final int overdueDays = (int) Math.max(0L, ChronoUnit.DAYS.between(dueDate, today));
			return new ActiveLoan(loan, book, loanDate, dueDate, overdueDays, policy.amountFor(overdueDays));
		}

	}

	public record PendingFine(UUID fine, UUID book, LocalDate issueDate, int overdueDays, BigDecimal dailyRate, BigDecimal amount) {
	}

	public record ReturnedLoan(UUID loan, UUID book, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, int overdueDays) {
	}

	public record Payment(UUID fine, UUID book, String reference, BigDecimal amount, Instant paidAt) {
	}

	public UserAccountDomain {
		activeLoans = List.copyOf(ObjectHelper.getDefaultValue(activeLoans, List.of()));
		pendingFines = List.copyOf(ObjectHelper.getDefaultValue(pendingFines, List.of()));
		returnedLoans = List.copyOf(ObjectHelper.getDefaultValue(returnedLoans, List.of()));
		payments = List.copyOf(ObjectHelper.getDefaultValue(payments, List.of()));
	}

	public BigDecimal pendingFinesTotal() {
		return sum(pendingFines, PendingFine::amount);
	}

	public BigDecimal accruingFinesTotal() {
		return sum(activeLoans, ActiveLoan::accruedFine);
	}

	private static <T> BigDecimal sum(final List<T> items, final Function<T, BigDecimal> amount) {
		return items.stream().map(amount).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
	}

}
