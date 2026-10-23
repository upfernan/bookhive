package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class FineCriteria {

	private final UUID user;
	private final String status;
	private final UUID loan;

	private FineCriteria(final UUID user, final String status, final UUID loan) {
		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.status = TextHelper.getDefaultValue(status);
		this.loan = UUIDHelper.getDefaultValue(loan);
	}

	public static FineCriteria pendingByUser(final UUID user) {
		return new FineCriteria(user, FineEntity.STATUS_PENDING, UUIDHelper.getDefaultValue());
	}

	public static FineCriteria ofLoan(final UUID loan) {
		return new FineCriteria(UUIDHelper.getDefaultValue(), TextHelper.getDefaultValue(), loan);
	}

	public UUID getUser() {
		return user;
	}

	public String getStatus() {
		return status;
	}

	public UUID getLoan() {
		return loan;
	}

	public boolean filtersByUser() {
		return !UUIDHelper.isDefaultValue(user);
	}

	public boolean filtersByStatus() {
		return !TextHelper.isDefaultValue(status);
	}

	public boolean filtersByLoan() {
		return !UUIDHelper.isDefaultValue(loan);
	}

}
