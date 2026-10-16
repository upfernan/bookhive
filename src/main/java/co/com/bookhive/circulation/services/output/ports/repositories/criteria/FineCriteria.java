package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class FineCriteria {

	private final UUID user;
	private final String status;

	private FineCriteria(final UUID user, final String status) {
		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.status = TextHelper.getDefaultValue(status);
	}

	public static FineCriteria pendingByUser(final UUID user) {
		return new FineCriteria(user, FineEntity.STATUS_PENDING);
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
