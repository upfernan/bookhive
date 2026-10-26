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
@Table(name = "fine_payment")
public class FinePaymentJpaEntity {

	@Id
	@Column(name = "id", updatable = false)
	private UUID id;

	@Column(name = "reference", updatable = false)
	private String reference;

	@Column(name = "fine_id", updatable = false)
	private UUID fine;

	@Column(name = "payer_id", updatable = false)
	private UUID payer;

	@Column(name = "institution_id", updatable = false)
	private UUID institution;

	@Column(name = "amount_in_cents", updatable = false)
	private long amountInCents;

	@Column(name = "currency", updatable = false)
	private String currency;

	@Column(name = "expires_at", updatable = false)
	private Instant expiresAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	public FinePaymentJpaEntity() {
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
