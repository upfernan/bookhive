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
import jakarta.persistence.Table;

@Entity
@Table(name = "prestamo")
public class LoanJpaEntity {

	public static final String STATUS_ACTIVE = "ACTIVO";
	public static final String STATUS_OVERDUE = "VENCIDO";
	public static final String STATUS_RETURNED = "DEVUELTO";

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "ejemplar_id", nullable = false)
	private UUID bookCopy;

	@Column(name = "usuario_id", nullable = false)
	private UUID user;

	@Column(name = "bibliotecario_id", nullable = false)
	private UUID librarian;

	@Column(name = "fecha_prestamo", nullable = false)
	private LocalDate loanDate;

	@Column(name = "fecha_vencimiento", nullable = false)
	private LocalDate dueDate;

	@Column(name = "estado", nullable = false, length = 20)
	private String status;

	@Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	public LoanJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBookCopy(UUIDHelper.getDefaultValue());
		setUser(UUIDHelper.getDefaultValue());
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

	public UUID getBookCopy() {
		return bookCopy;
	}

	public void setBookCopy(final UUID bookCopy) {
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
	}

	public UUID getUser() {
		return user;
	}

	public void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
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
