package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "institution_parameters")
public class InstitutionParametersJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "loan_days")
	private int loanDays;

	@Column(name = "max_concurrent_loans")
	private int maxConcurrentLoans;

	@Column(name = "daily_fine_rate")
	private BigDecimal dailyFineRate;

	@Column(name = "time_zone")
	private String timeZone;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public InstitutionParametersJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoanDays(0);
		setMaxConcurrentLoans(0);
		setDailyFineRate(BigDecimal.ZERO);
		setTimeZone("");
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

	public BigDecimal getDailyFineRate() {
		return dailyFineRate;
	}

	public void setDailyFineRate(final BigDecimal dailyFineRate) {
		this.dailyFineRate = ObjectHelper.getDefaultValue(dailyFineRate, BigDecimal.ZERO);
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(final String timeZone) {
		this.timeZone = TextHelper.getDefaultValue(timeZone);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
