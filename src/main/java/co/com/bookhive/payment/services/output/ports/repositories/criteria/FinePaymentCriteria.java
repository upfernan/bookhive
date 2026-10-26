package co.com.bookhive.payment.services.output.ports.repositories.criteria;

import java.time.Instant;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class FinePaymentCriteria {

	private final String reference;
	private final UUID fine;
	private final UUID payer;
	private final Instant expiresAfter;
	private final Instant createdFrom;
	private final Instant createdUntil;
	private final boolean onlyUnresolved;

	private FinePaymentCriteria(final Builder builder) {
		super();
		this.reference = TextHelper.getDefaultValue(builder.reference).trim();
		this.fine = UUIDHelper.getDefaultValue(builder.fine);
		this.payer = UUIDHelper.getDefaultValue(builder.payer);
		this.expiresAfter = DateHelper.getDefaultValue(builder.expiresAfter);
		this.createdFrom = DateHelper.getDefaultValue(builder.createdFrom);
		this.createdUntil = DateHelper.getDefaultValue(builder.createdUntil);
		this.onlyUnresolved = builder.onlyUnresolved;
	}

	public static FinePaymentCriteria withReference(final String reference) {
		final Builder builder = new Builder();
		builder.reference = reference;
		return new FinePaymentCriteria(builder);
	}

	public static FinePaymentCriteria openFor(final UUID fine, final UUID payer, final Instant expiresAfter) {
		final Builder builder = new Builder();
		builder.fine = fine;
		builder.payer = payer;
		builder.expiresAfter = expiresAfter;
		builder.onlyUnresolved = true;
		return new FinePaymentCriteria(builder);
	}

	public static FinePaymentCriteria unresolvedCreatedBetween(final Instant from, final Instant until) {
		final Builder builder = new Builder();
		builder.createdFrom = from;
		builder.createdUntil = until;
		builder.onlyUnresolved = true;
		return new FinePaymentCriteria(builder);
	}

	public String getReference() {
		return reference;
	}

	public UUID getFine() {
		return fine;
	}

	public UUID getPayer() {
		return payer;
	}

	public Instant getExpiresAfter() {
		return expiresAfter;
	}

	public Instant getCreatedFrom() {
		return createdFrom;
	}

	public Instant getCreatedUntil() {
		return createdUntil;
	}

	public boolean onlyUnresolved() {
		return onlyUnresolved;
	}

	public boolean filtersByReference() {
		return !TextHelper.isDefaultValue(reference);
	}

	public boolean filtersByFine() {
		return !UUIDHelper.isDefaultValue(fine);
	}

	public boolean filtersByPayer() {
		return !UUIDHelper.isDefaultValue(payer);
	}

	public boolean filtersByExpiration() {
		return !DateHelper.isDefaultValue(expiresAfter);
	}

	public boolean filtersByCreation() {
		return !DateHelper.isDefaultValue(createdFrom) && !DateHelper.isDefaultValue(createdUntil);
	}

	private static final class Builder {

		private String reference;
		private UUID fine;
		private UUID payer;
		private Instant expiresAfter;
		private Instant createdFrom;
		private Instant createdUntil;
		private boolean onlyUnresolved;

	}

}
