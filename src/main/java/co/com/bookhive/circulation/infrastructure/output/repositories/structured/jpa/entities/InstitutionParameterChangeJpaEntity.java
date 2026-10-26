package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "institution_parameter_change")
public class InstitutionParameterChangeJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "parameter", updatable = false)
	private String parameter;

	@Column(name = "previous_value", updatable = false)
	private String previousValue;

	@Column(name = "new_value", updatable = false)
	private String newValue;

	@Column(name = "changed_by", updatable = false)
	private UUID changedBy;

	@Column(name = "changed_at", insertable = false, updatable = false)
	private Instant changedAt;

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(final String parameter) {
		this.parameter = parameter;
	}

	public String getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(final String previousValue) {
		this.previousValue = previousValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(final String newValue) {
		this.newValue = newValue;
	}

	public UUID getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(final UUID changedBy) {
		this.changedBy = changedBy;
	}

	public Instant getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(final Instant changedAt) {
		this.changedAt = changedAt;
	}

}
