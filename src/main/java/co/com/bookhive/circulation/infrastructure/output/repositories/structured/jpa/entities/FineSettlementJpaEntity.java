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
@Table(name = "fine_settlement")
public class FineSettlementJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "fine_id", updatable = false)
	private UUID fine;

	@Column(name = "payment_reference", updatable = false)
	private String paymentReference;

	@Column(name = "amount", updatable = false)
	private BigDecimal amount;

	@Column(name = "settled_at", insertable = false, updatable = false)
	private Instant settledAt;

	public FineSettlementJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setFine(UUIDHelper.getDefaultValue());
		setPaymentReference(TextHelper.getDefaultValue());
		setAmount(BigDecimal.ZERO);
		setSettledAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public UUID getFine() {
		return fine;
	}

	public void setFine(final UUID fine) {
		this.fine = UUIDHelper.getDefaultValue(fine);
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	public void setPaymentReference(final String paymentReference) {
		this.paymentReference = TextHelper.getDefaultValue(paymentReference).trim();
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = ObjectHelper.getDefaultValue(amount, BigDecimal.ZERO);
	}

	public Instant getSettledAt() {
		return settledAt;
	}

	public void setSettledAt(final Instant settledAt) {
		this.settledAt = DateHelper.getDefaultValue(settledAt);
	}

}
