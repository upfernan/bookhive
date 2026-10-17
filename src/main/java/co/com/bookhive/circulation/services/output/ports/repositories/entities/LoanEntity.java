package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class LoanEntity {

	public static final String STATUS_ACTIVE = "ACTIVO";
	public static final String STATUS_OVERDUE = "VENCIDO";
	public static final String STATUS_RETURNED = "DEVUELTO";

	private UUID id;
	private BookCopyEntity bookCopy;
	private UserEntity user;
	private UUID librarian;
	private LocalDate loanDate;
	private LocalDate dueDate;
	private String status;

	private Instant createdAt;

	public LoanEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBookCopy(new BookCopyEntity());
		setUser(new UserEntity());
		setLibrarian(UUIDHelper.getDefaultValue());
		setLoanDate(DateHelper.getDefaultValue());
		setDueDate(DateHelper.getDefaultValue());
		setStatus(STATUS_ACTIVE);
		setCreatedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public BookCopyEntity getBookCopy() {
		return bookCopy;
	}

	public void setBookCopy(final BookCopyEntity bookCopy) {
		this.bookCopy = ObjectHelper.getDefaultValue(bookCopy, new BookCopyEntity());
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(final UserEntity user) {
		this.user = ObjectHelper.getDefaultValue(user, new UserEntity());
	}

	public UUID getLibrarian() {
		return librarian;
	}

	public void setLibrarian(final UUID librarian) {
		this.librarian = UUIDHelper.getDefaultValue(librarian);
	}

	public LocalDate getLoanDate() {
		return loanDate;
	}

	public void setLoanDate(final LocalDate loanDate) {
		this.loanDate = DateHelper.getDefaultValue(loanDate);
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = DateHelper.getDefaultValue(dueDate);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = TextHelper.isDefaultValue(status) ? STATUS_ACTIVE : status.trim();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
