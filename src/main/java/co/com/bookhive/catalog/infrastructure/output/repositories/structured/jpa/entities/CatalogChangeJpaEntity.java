package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalog_change")
public class CatalogChangeJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "book_id", updatable = false)
	private UUID book;

	@Column(name = "changed_at", updatable = false)
	private Instant changedAt;

	public CatalogChangeJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setBook(UUIDHelper.getDefaultValue());
		setChangedAt(DateHelper.getDefaultTimestamp());
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

	public Instant getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(final Instant changedAt) {
		this.changedAt = DateHelper.getDefaultValue(changedAt);
	}

}
