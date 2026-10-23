package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class LoanReturnEntity {

	private UUID id;
	private UUID loan;
	private LocalDate returnDate;
	private int overdueDays;
	private UUID librarian;
	private Instant createdAt;

	public LoanReturnEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoan(UUIDHelper.getDefaultValue());
		setReturnDate(DateHelper.getDefaultValue());
		setOverdueDays(0);
		setLibrarian(UUIDHelper.getDefaultValue());
		setCreatedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public UUID getLoan() {
		return loan;
	}

	public void setLoan(final UUID loan) {
		this.loan = UUIDHelper.getDefaultValue(loan);
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(final LocalDate returnDate) {
		this.returnDate = DateHelper.getDefaultValue(returnDate);
	}

	public int getOverdueDays() {
		return overdueDays;
	}

	public void setOverdueDays(final int overdueDays) {
		this.overdueDays = Math.max(overdueDays, 0);
	}

	public UUID getLibrarian() {
		return librarian;
	}

	public void setLibrarian(final UUID librarian) {
		this.librarian = UUIDHelper.getDefaultValue(librarian);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
