package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation")
public class ReservationJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "book_id", updatable = false)
	private UUID book;

	@Column(name = "user_id", updatable = false)
	private UUID user;

	@Column(name = "status")
	private String status;

	@Column(name = "requested_at", updatable = false)
	private Instant requestedAt;

	@Column(name = "book_copy_id")
	private UUID bookCopy;

	@Column(name = "assigned_at")
	private Instant assignedAt;

	@Column(name = "pickup_deadline")
	private LocalDate pickupDeadline;

	public ReservationJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBook(UUIDHelper.getDefaultValue());
		setUser(UUIDHelper.getDefaultValue());
		setStatus(TextHelper.getDefaultValue());
		setRequestedAt(DateHelper.getDefaultTimestamp());
		setBookCopy(UUIDHelper.getDefaultValue());
		setAssignedAt(DateHelper.getDefaultTimestamp());
		setPickupDeadline(DateHelper.getDefaultValue());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public UUID getBook() {
		return book;
	}

	public void setBook(final UUID book) {
		this.book = UUIDHelper.getDefaultValue(book);
	}

	public UUID getUser() {
		return user;
	}

	public void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = TextHelper.getDefaultValue(status);
	}

	public Instant getRequestedAt() {
		return DateHelper.getDefaultValue(requestedAt);
	}

	public void setRequestedAt(final Instant requestedAt) {
		this.requestedAt = DateHelper.isDefaultValue(requestedAt) ? null : requestedAt;
	}

	public UUID getBookCopy() {
		return UUIDHelper.getDefaultValue(bookCopy);
	}

	public void setBookCopy(final UUID bookCopy) {
		this.bookCopy = UUIDHelper.isDefaultValue(bookCopy) ? null : bookCopy;
	}

	public Instant getAssignedAt() {
		return DateHelper.getDefaultValue(assignedAt);
	}

	public void setAssignedAt(final Instant assignedAt) {
		this.assignedAt = DateHelper.isDefaultValue(assignedAt) ? null : assignedAt;
	}

	public LocalDate getPickupDeadline() {
		return DateHelper.getDefaultValue(pickupDeadline);
	}

	public void setPickupDeadline(final LocalDate pickupDeadline) {
		this.pickupDeadline = DateHelper.isDefaultValue(pickupDeadline) ? null : pickupDeadline;
	}

}
