package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class InstitutionParametersEntity {

	public static final String DEFAULT_TIME_ZONE = "America/Bogota";

	private UUID id;
	private int loanDays;
	private int maxConcurrentLoans;
	private BigDecimal dailyFineRate;
	private String timeZone;

	private Instant createdAt;

	public InstitutionParametersEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoanDays(0);
		setMaxConcurrentLoans(0);
		setDailyFineRate(BigDecimal.ZERO);
		setTimeZone(DEFAULT_TIME_ZONE);
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

	public BigDecimal getDailyFineRate() {
		return dailyFineRate;
	}

	public void setDailyFineRate(final BigDecimal dailyFineRate) {
		this.dailyFineRate = Optional.ofNullable(dailyFineRate).map(BigDecimal.ZERO::max).orElse(BigDecimal.ZERO);
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(final String timeZone) {
		this.timeZone = Optional.ofNullable(timeZone)
				.map(String::trim)
				.filter(zone -> !zone.isEmpty())
				.orElse(DEFAULT_TIME_ZONE);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
