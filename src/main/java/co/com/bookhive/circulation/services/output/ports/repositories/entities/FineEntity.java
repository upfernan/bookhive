package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class FineEntity {

	public static final String STATUS_PENDING = "PENDIENTE";
	public static final String STATUS_PAID = "PAGADA";
	public static final String STATUS_WAIVED = "CONDONADA";

	private UUID id;
	private LoanEntity loan;
	private UserEntity user;
	private LocalDate issueDate;
	private int overdueDays;
	private BigDecimal dailyRate;
	private BigDecimal totalAmount;
	private String status;

	private Instant createdAt;

	public FineEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoan(new LoanEntity());
		setUser(new UserEntity());
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

	public LoanEntity getLoan() {
		return loan;
	}

	public void setLoan(final LoanEntity loan) {
		this.loan = ObjectHelper.getDefaultValue(loan, new LoanEntity());
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(final UserEntity user) {
		this.user = ObjectHelper.getDefaultValue(user, new UserEntity());
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
		this.status = TextHelper.isDefaultValue(status) ? STATUS_PENDING : TextHelper.getDefaultValue(status);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
