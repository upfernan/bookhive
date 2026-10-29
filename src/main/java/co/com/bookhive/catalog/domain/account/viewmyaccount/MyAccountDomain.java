package co.com.bookhive.catalog.domain.account.viewmyaccount;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import co.com.bookhive.catalog.domain.reservation.listmyreservations.MyReservationDomain;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record MyAccountDomain(
		List<Loan> activeLoans,
		List<MyReservationDomain> reservations,
		List<Fine> pendingFines,
		BigDecimal pendingFinesTotal,
		BigDecimal accruingFinesTotal,
		List<Return> loanHistory,
		List<Payment> payments) {

	public record Book(UUID id, String title, String authors) {

		public Book {
			id = UUIDHelper.getDefaultValue(id);
			title = TextHelper.getDefaultValue(title);
			authors = TextHelper.getDefaultValue(authors);
		}

		public static Book unnamed(final UUID id) {
			return new Book(id, "", "");
		}

	}

	public record Loan(UUID loan, Book book, LocalDate loanDate, LocalDate dueDate, int overdueDays, BigDecimal accruedFine) {
	}

	public record Fine(UUID fine, Book book, LocalDate issueDate, int overdueDays, BigDecimal dailyRate, BigDecimal amount) {
	}

	public record Return(UUID loan, Book book, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, int overdueDays) {
	}

	public record Payment(UUID fine, Book book, String reference, BigDecimal amount, Instant paidAt) {
	}

	public MyAccountDomain {
		activeLoans = List.copyOf(ObjectHelper.getDefaultValue(activeLoans, List.of()));
		reservations = List.copyOf(ObjectHelper.getDefaultValue(reservations, List.of()));
		pendingFines = List.copyOf(ObjectHelper.getDefaultValue(pendingFines, List.of()));
		pendingFinesTotal = ObjectHelper.getDefaultValue(pendingFinesTotal, BigDecimal.ZERO);
		accruingFinesTotal = ObjectHelper.getDefaultValue(accruingFinesTotal, BigDecimal.ZERO);
		loanHistory = List.copyOf(ObjectHelper.getDefaultValue(loanHistory, List.of()));
		payments = List.copyOf(ObjectHelper.getDefaultValue(payments, List.of()));
	}

	public List<UUID> books() {
		return Stream.of(
				activeLoans.stream().map(Loan::book),
				pendingFines.stream().map(Fine::book),
				loanHistory.stream().map(Return::book),
				payments.stream().map(Payment::book))
				.flatMap(stream -> stream)
				.map(Book::id)
				.distinct()
				.toList();
	}

	public MyAccountDomain with(final Map<UUID, Book> books, final List<MyReservationDomain> userReservations) {
		return new MyAccountDomain(
				activeLoans.stream().map(item -> new Loan(item.loan(), named(books, item.book()), item.loanDate(),
						item.dueDate(), item.overdueDays(), item.accruedFine())).toList(),
				userReservations,
				pendingFines.stream().map(item -> new Fine(item.fine(), named(books, item.book()), item.issueDate(),
						item.overdueDays(), item.dailyRate(), item.amount())).toList(),
				pendingFinesTotal,
				accruingFinesTotal,
				loanHistory.stream().map(item -> new Return(item.loan(), named(books, item.book()), item.loanDate(),
						item.dueDate(), item.returnDate(), item.overdueDays())).toList(),
				payments.stream().map(item -> new Payment(item.fine(), named(books, item.book()), item.reference(),
						item.amount(), item.paidAt())).toList());
	}

	private static Book named(final Map<UUID, Book> books, final Book book) {
		return books.getOrDefault(book.id(), book);
	}

}
