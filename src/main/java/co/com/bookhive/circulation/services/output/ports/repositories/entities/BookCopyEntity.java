package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class BookCopyEntity {

	public static final String STATUS_AVAILABLE = "DISPONIBLE";
	public static final String STATUS_LENT = "PRESTADO";
	public static final String STATUS_RESERVED = "RESERVADO";
	public static final String STATUS_WITHDRAWN = "BAJA";

	private UUID id;
	private UUID book;
	private UUID branch;
	private String barcode;
	private String status;
	private boolean lendable;

	private Instant createdAt;

	public BookCopyEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBook(UUIDHelper.getDefaultValue());
		setBranch(UUIDHelper.getDefaultValue());
		setBarcode(TextHelper.getDefaultValue());
		setStatus(STATUS_AVAILABLE);
		setLendable(false);
		setCreatedAt(DateHelper.getDefaultTimestamp());
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

	public UUID getBranch() {
		return branch;
	}

	public void setBranch(final UUID branch) {
		this.branch = UUIDHelper.getDefaultValue(branch);
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(final String barcode) {
		this.barcode = TextHelper.getDefaultValue(barcode);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = TextHelper.isDefaultValue(status) ? STATUS_AVAILABLE : TextHelper.getDefaultValue(status);
	}

	public boolean isLendable() {
		return lendable;
	}

	public void setLendable(final boolean lendable) {
		this.lendable = lendable;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
