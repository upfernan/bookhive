package co.com.bookhive.payment.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class FinePaymentEventEntity {

	private UUID id;
	private UUID payment;
	private String transaction;
	private String status;
	private long amountInCents;
	private String source;
	private Instant receivedAt;

	public FinePaymentEventEntity() {
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
