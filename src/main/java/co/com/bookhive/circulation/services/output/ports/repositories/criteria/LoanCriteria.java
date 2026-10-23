package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class LoanCriteria {

	private final UUID user;
	private final String status;
	private final LocalDate dueDate;

	private LoanCriteria(final UUID user, final String status, final LocalDate dueDate) {
		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.status = TextHelper.getDefaultValue(status);
		this.dueDate = DateHelper.getDefaultValue(dueDate);
	}

	public static LoanCriteria activeByUser(final UUID user) {
		return new LoanCriteria(user, LoanEntity.STATUS_ACTIVE, DateHelper.getDefaultValue());
	}

	public static LoanCriteria activeDueOn(final LocalDate dueDate) {
		return new LoanCriteria(UUIDHelper.getDefaultValue(), LoanEntity.STATUS_ACTIVE, dueDate);
	}

	public UUID getUser() {
		return user;
	}

	public String getStatus() {
		return status;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public boolean filtersByUser() {
		return !UUIDHelper.isDefaultValue(user);
	}

	public boolean filtersByStatus() {
		return !TextHelper.isDefaultValue(status);
	}

	public boolean filtersByDueDate() {
		return !DateHelper.isDefaultValue(dueDate);
	}

}
