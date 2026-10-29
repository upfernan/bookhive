package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class FineCriteria {

	private final UUID id;
	private final UUID user;
	private final UUID loan;
	private final boolean onlyPending;

	private FineCriteria(final UUID id, final UUID user, final UUID loan, final boolean onlyPending) {
		super();
		this.id = UUIDHelper.getDefaultValue(id);
		this.user = UUIDHelper.getDefaultValue(user);
		this.loan = UUIDHelper.getDefaultValue(loan);
		this.onlyPending = onlyPending;
	}

	public static FineCriteria pendingByUser(final UUID user) {
		return new FineCriteria(UUIDHelper.getDefaultValue(), user, UUIDHelper.getDefaultValue(), true);
	}

	public static FineCriteria ofLoan(final UUID loan) {
		return new FineCriteria(UUIDHelper.getDefaultValue(), UUIDHelper.getDefaultValue(), loan, false);
	}

	public static FineCriteria ofUser(final UUID fine, final UUID user) {
		return new FineCriteria(fine, user, UUIDHelper.getDefaultValue(), false);
	}

	public static FineCriteria withId(final UUID fine) {
		return new FineCriteria(fine, UUIDHelper.getDefaultValue(), UUIDHelper.getDefaultValue(), false);
	}

	public UUID getId() {
		return id;
	}

	public UUID getUser() {
		return user;
	}

	public UUID getLoan() {
		return loan;
	}

	public boolean onlyPending() {
		return onlyPending;
	}

	public boolean filtersById() {
		return !UUIDHelper.isDefaultValue(id);
	}

	public boolean filtersByUser() {
		return !UUIDHelper.isDefaultValue(user);
	}

	public boolean filtersByLoan() {
		return !UUIDHelper.isDefaultValue(loan);
	}

}
