package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities;

import java.util.UUID;

import org.hibernate.annotations.Immutable;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "book")
public class BookJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "title")
	private String title;

	@Column(name = "status")
	private String status;

	public BookJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setTitle(TextHelper.getDefaultValue());
		setStatus(TextHelper.getDefaultValue());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = TextHelper.getDefaultValue(title);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = TextHelper.getDefaultValue(status);
	}

}
