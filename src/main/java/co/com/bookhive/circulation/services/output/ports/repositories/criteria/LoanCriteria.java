package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class LoanCriteria {

	private final UUID user;
	private final String status;

	private LoanCriteria(final UUID user, final String status) {
		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.status = TextHelper.getDefaultValue(status);
	}

	public static LoanCriteria activeByUser(final UUID user) {
		return new LoanCriteria(user, LoanEntity.STATUS_ACTIVE);
	}

	public UUID getUser() {
		return user;
	}

	public String getStatus() {
		return status;
	}

	public boolean filtersByUser() {
		return !UUIDHelper.isDefaultValue(user);
	}

	public boolean filtersByStatus() {
		return !TextHelper.isDefaultValue(status);
	}

}
