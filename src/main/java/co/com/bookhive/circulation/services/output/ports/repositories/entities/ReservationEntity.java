package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class ReservationEntity {

	public static final String STATUS_WAITING = "EN_ESPERA";
	public static final String STATUS_ASSIGNED = "ASIGNADA";
	public static final String STATUS_PICKED_UP = "RETIRADA";
	public static final String STATUS_EXPIRED = "VENCIDA";
	public static final String STATUS_CANCELLED = "CANCELADA";

	private UUID id;
	private UUID book;
	private UUID user;
	private String status;
	private Instant requestedAt;
	private UUID bookCopy;
	private Instant assignedAt;
	private LocalDate pickupDeadline;

	public ReservationEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBook(UUIDHelper.getDefaultValue());
		setUser(UUIDHelper.getDefaultValue());
		setStatus(STATUS_WAITING);
		setRequestedAt(DateHelper.getDefaultTimestamp());
		setBookCopy(UUIDHelper.getDefaultValue());
		setAssignedAt(DateHelper.getDefaultTimestamp());
		setPickupDeadline(DateHelper.getDefaultValue());
	}

	public boolean isWaiting() {
		return STATUS_WAITING.equals(status);
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
		this.status = TextHelper.isDefaultValue(status) ? STATUS_WAITING : status.trim();
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(final Instant requestedAt) {
		this.requestedAt = DateHelper.getDefaultValue(requestedAt);
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public void setBookCopy(final UUID bookCopy) {
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(final Instant assignedAt) {
		this.assignedAt = DateHelper.getDefaultValue(assignedAt);
	}

	public LocalDate getPickupDeadline() {
		return pickupDeadline;
	}

	public void setPickupDeadline(final LocalDate pickupDeadline) {
		this.pickupDeadline = DateHelper.getDefaultValue(pickupDeadline);
	}

}
