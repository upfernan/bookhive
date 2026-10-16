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
import jakarta.persistence.Table;

@Entity
@Table(name = "multa")
public class FineJpaEntity {

	public static final String STATUS_PENDING = "PENDIENTE";
	public static final String STATUS_PAID = "PAGADA";

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "prestamo_id", nullable = false, updatable = false)
	private UUID loan;

	@Column(name = "usuario_id", nullable = false, updatable = false)
	private UUID user;

	@Column(name = "fecha_generacion", nullable = false, updatable = false)
	private LocalDate issueDate;

	@Column(name = "dias_retraso", nullable = false, updatable = false)
	private int overdueDays;

	@Column(name = "tarifa_diaria", nullable = false, updatable = false, precision = 12, scale = 2)
	private BigDecimal dailyRate;

	@Column(name = "valor_total", nullable = false, updatable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "estado", nullable = false, length = 20)
	private String status;

	@Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
	private Instant createdAt;

	public FineJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setLoan(UUIDHelper.getDefaultValue());
		setUser(UUIDHelper.getDefaultValue());
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

	public UUID getLoan() {
		return loan;
	}

	public void setLoan(final UUID loan) {
		this.loan = UUIDHelper.getDefaultValue(loan);
	}

	public UUID getUser() {
		return user;
	}

	public void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
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
