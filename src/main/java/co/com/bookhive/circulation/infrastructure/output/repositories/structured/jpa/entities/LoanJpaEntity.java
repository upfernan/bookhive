package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan")
public class LoanJpaEntity {

	public static final String STATUS_ACTIVE = "ACTIVO";
	public static final String STATUS_OVERDUE = "VENCIDO";
	public static final String STATUS_RETURNED = "DEVUELTO";

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "book_copy_id", updatable = false)
	private BookCopyJpaEntity bookCopy;

	@ManyToOne
	@JoinColumn(name = "user_id", updatable = false)
	private UserJpaEntity user;

	@Column(name = "librarian_id", updatable = false)
	private UUID librarian;

	@Column(name = "loan_date", updatable = false)
	private LocalDate loanDate;

	@Column(name = "due_date", updatable = false)
	private LocalDate dueDate;

	@Column(name = "status", updatable = false)
	private String status;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public LoanJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBookCopy(new BookCopyJpaEntity());
		setUser(new UserJpaEntity());
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

	public BookCopyJpaEntity getBookCopy() {
		return bookCopy;
	}

	public void setBookCopy(final BookCopyJpaEntity bookCopy) {
		this.bookCopy = ObjectHelper.getDefaultValue(bookCopy, new BookCopyJpaEntity());
	}

	public UserJpaEntity getUser() {
		return user;
	}

	public void setUser(final UserJpaEntity user) {
		this.user = ObjectHelper.getDefaultValue(user, new UserJpaEntity());
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
		this.status = ObjectHelper.getDefaultValue(status, STATUS_ACTIVE).trim();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
