package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "book_copy")
public class BookCopyJpaEntity {

	public static final String STATUS_AVAILABLE = "DISPONIBLE";
	public static final String STATUS_LENT = "PRESTADO";
	public static final String STATUS_RESERVED = "RESERVADO";

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "book_id")
	private UUID book;

	@Column(name = "branch_id")
	private UUID branch;

	@Column(name = "barcode")
	private String barcode;

	@Column(name = "status")
	private String status;

	@Column(name = "lendable")
	private boolean lendable;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public BookCopyJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBook(UUIDHelper.getDefaultValue());
		setBranch(UUIDHelper.getDefaultValue());
		setBarcode("");
		setStatus(STATUS_AVAILABLE);
		setLendable(true);
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
		this.barcode = ObjectHelper.getDefaultValue(barcode, "").trim();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = ObjectHelper.getDefaultValue(status, STATUS_AVAILABLE).trim();
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
