package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fine")
public class FineJpaEntity {

	public static final String STATUS_PENDING = "PENDIENTE";
	public static final String STATUS_PAID = "PAGADA";

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "loan_id", updatable = false)
	private LoanJpaEntity loan;

	@ManyToOne
	@JoinColumn(name = "user_id", updatable = false)
	private UserJpaEntity user;

	@Column(name = "issue_date", updatable = false)
	private LocalDate issueDate;

	@Column(name = "overdue_days", updatable = false)
	private int overdueDays;

	@Column(name = "daily_rate", updatable = false)
	private BigDecimal dailyRate;

	@Column(name = "total_amount", updatable = false)
	private BigDecimal totalAmount;

	@Column(name = "status", updatable = false)
	private String status;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public FineJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoan(new LoanJpaEntity());
		setUser(new UserJpaEntity());
		setIssueDate(DateHelper.getDefaultValue());
		setOverdueDays(0);
		setDailyRate(BigDecimal.ZERO);
		setTotalAmount(BigDecimal.ZERO);
		setStatus(STATUS_PENDING);
		setCreatedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public LoanJpaEntity getLoan() {
		return loan;
	}

	public void setLoan(final LoanJpaEntity loan) {
		this.loan = ObjectHelper.getDefaultValue(loan, new LoanJpaEntity());
	}

	public UserJpaEntity getUser() {
		return user;
	}

	public void setUser(final UserJpaEntity user) {
		this.user = ObjectHelper.getDefaultValue(user, new UserJpaEntity());
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(final LocalDate issueDate) {
		this.issueDate = DateHelper.getDefaultValue(issueDate);
	}

	public int getOverdueDays() {
		return overdueDays;
	}

	public void setOverdueDays(final int overdueDays) {
		this.overdueDays = Math.max(overdueDays, 0);
	}

	public BigDecimal getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(final BigDecimal dailyRate) {
		this.dailyRate = ObjectHelper.getDefaultValue(dailyRate, BigDecimal.ZERO);
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(final BigDecimal totalAmount) {
		this.totalAmount = ObjectHelper.getDefaultValue(totalAmount, BigDecimal.ZERO);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = ObjectHelper.getDefaultValue(status, STATUS_PENDING).trim();
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
