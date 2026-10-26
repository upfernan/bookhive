package co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fine_payment_event")
public class FinePaymentEventJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "payment_id", updatable = false)
	private UUID payment;

	@Column(name = "transaction_id", updatable = false)
	private String transaction;

	@Column(name = "status", updatable = false)
	private String status;

	@Column(name = "amount_in_cents", updatable = false)
	private long amountInCents;

	@Column(name = "source", updatable = false)
	private String source;

	@Column(name = "received_at", insertable = false, updatable = false)
	private Instant receivedAt;

	public FinePaymentEventJpaEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setPayment(UUIDHelper.getDefaultValue());
		setTransaction(TextHelper.getDefaultValue());
		setStatus(TextHelper.getDefaultValue());
		setAmountInCents(0L);
		setSource(TextHelper.getDefaultValue());
		setReceivedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public UUID getPayment() {
		return payment;
	}

	public void setPayment(final UUID payment) {
		this.payment = UUIDHelper.getDefaultValue(payment);
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(final String transaction) {
		this.transaction = TextHelper.getDefaultValue(transaction).trim();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = TextHelper.getDefaultValue(status).trim();
	}

	public long getAmountInCents() {
		return amountInCents;
	}

	public void setAmountInCents(final long amountInCents) {
		this.amountInCents = Math.max(amountInCents, 0L);
	}

	public String getSource() {
		return source;
	}

	public void setSource(final String source) {
		this.source = TextHelper.getDefaultValue(source).trim();
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(final Instant receivedAt) {
		this.receivedAt = DateHelper.getDefaultValue(receivedAt);
	}

}
