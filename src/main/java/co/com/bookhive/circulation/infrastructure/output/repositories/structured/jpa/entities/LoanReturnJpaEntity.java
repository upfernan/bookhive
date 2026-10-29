package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_return")
public class LoanReturnJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "loan_id", updatable = false)
	private UUID loan;

	@Column(name = "return_date", updatable = false)
	private LocalDate returnDate;

	@Column(name = "overdue_days", updatable = false)
	private int overdueDays;

	@Column(name = "librarian_id", updatable = false)
	private UUID librarian;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public LoanReturnJpaEntity() {
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
