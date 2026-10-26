package co.com.bookhive.payment.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public class FinePaymentEntity {

	private UUID id;
	private String reference;
	private UUID fine;
	private UUID payer;
	private UUID institution;
	private long amountInCents;
	private String currency;
	private Instant expiresAt;
	private Instant createdAt;

	public FinePaymentEntity() {
		super();
		setId(UUIDHelper.getDefaultValue());
		setReference(TextHelper.getDefaultValue());
		setFine(UUIDHelper.getDefaultValue());
		setPayer(UUIDHelper.getDefaultValue());
		setInstitution(UUIDHelper.getDefaultValue());
		setAmountInCents(0L);
		setCurrency(TextHelper.getDefaultValue());
		setExpiresAt(DateHelper.getDefaultTimestamp());
		setCreatedAt(DateHelper.getDefaultTimestamp());
	}

	public UUID getId() {
		return id;
	}

	public void setId(final UUID id) {
		this.id = UUIDHelper.getDefaultValue(id);
	}

	public String getReference() {
		return reference;
	}

	public void setReference(final String reference) {
		this.reference = TextHelper.getDefaultValue(reference).trim();
	}

	public UUID getFine() {
		return fine;
	}

	public void setFine(final UUID fine) {
		this.fine = UUIDHelper.getDefaultValue(fine);
	}

	public UUID getPayer() {
		return payer;
	}

	public void setPayer(final UUID payer) {
		this.payer = UUIDHelper.getDefaultValue(payer);
	}

	public UUID getInstitution() {
		return institution;
	}

	public void setInstitution(final UUID institution) {
		this.institution = UUIDHelper.getDefaultValue(institution);
	}

	public long getAmountInCents() {
		return amountInCents;
	}

	public void setAmountInCents(final long amountInCents) {
		this.amountInCents = Math.max(amountInCents, 0L);
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(final String currency) {
		this.currency = TextHelper.getDefaultValue(currency).trim();
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(final Instant expiresAt) {
		this.expiresAt = DateHelper.getDefaultValue(expiresAt);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Instant createdAt) {
		this.createdAt = DateHelper.getDefaultValue(createdAt);
	}

}
