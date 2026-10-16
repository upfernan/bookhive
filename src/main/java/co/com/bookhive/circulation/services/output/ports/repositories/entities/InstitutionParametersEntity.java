package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class InstitutionParametersEntity {

	private UUID id;
	private int loanDays;
	private int maxConcurrentLoans;

	private Instant createdAt;

	public InstitutionParametersEntity() {
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
		this.loanDays = Math.max(loanDays, 0);
	}

	public int getMaxConcurrentLoans() {
		return maxConcurrentLoans;
	}

	public void setMaxConcurrentLoans(final int maxConcurrentLoans) {
		this.maxConcurrentLoans = Math.max(maxConcurrentLoans, 0);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
