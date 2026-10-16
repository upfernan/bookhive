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
@Table(name = "ejemplar")
public class BookCopyJpaEntity {

	public static final String STATUS_AVAILABLE = "DISPONIBLE";
	public static final String STATUS_LENT = "PRESTADO";
	public static final String STATUS_RESERVED = "RESERVADO";

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "libro_id", nullable = false)
	private UUID book;

	@Column(name = "sede_id", nullable = false)
	private UUID branch;

	@Column(name = "codigo_barras", nullable = false, unique = true, length = 50)
	private String barcode;

	@Column(name = "estado", nullable = false, length = 20)
	private String status;

	@Column(name = "prestable", nullable = false)
	private boolean lendable;

	@Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
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
