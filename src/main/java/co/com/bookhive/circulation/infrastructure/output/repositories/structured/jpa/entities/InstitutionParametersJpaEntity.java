package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parametros_institucion")
public class InstitutionParametersJpaEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "dias_prestamo", nullable = false)
	private int loanDays;

	@Column(name = "maximo_prestamos_simultaneos", nullable = false)
	private int maxConcurrentLoans;

	@Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	public InstitutionParametersJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoanDays(0);
		setMaxConcurrentLoans(0);
		setCreatedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public int getLoanDays() {
		return loanDays;
	}

	public void setLoanDays(final int loanDays) {
		this.loanDays = loanDays;
	}

	public int getMaxConcurrentLoans() {
		return maxConcurrentLoans;
	}

	public void setMaxConcurrentLoans(final int maxConcurrentLoans) {
		this.maxConcurrentLoans = maxConcurrentLoans;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
