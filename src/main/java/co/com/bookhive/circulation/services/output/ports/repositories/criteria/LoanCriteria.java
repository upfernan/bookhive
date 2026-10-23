package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class LoanCriteria {

	private final UUID user;
	private final LocalDate dueDate;
	private final UUID bookCopy;
	private final boolean onlyOpen;

	private LoanCriteria(final UUID user, final LocalDate dueDate, final UUID bookCopy, final boolean onlyOpen) {
		super();
		this.user = UUIDHelper.getDefaultValue(user);
		this.dueDate = DateHelper.getDefaultValue(dueDate);
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		this.onlyOpen = onlyOpen;
	}

	public static LoanCriteria activeByUser(final UUID user) {
		return new LoanCriteria(user, DateHelper.getDefaultValue(), UUIDHelper.getDefaultValue(), true);
	}

	public static LoanCriteria activeDueOn(final LocalDate dueDate) {
		return new LoanCriteria(UUIDHelper.getDefaultValue(), dueDate, UUIDHelper.getDefaultValue(), true);
	}

	public static LoanCriteria openForBookCopy(final UUID bookCopy) {
		return new LoanCriteria(UUIDHelper.getDefaultValue(), DateHelper.getDefaultValue(), bookCopy, true);
	}

	public UUID getUser() {
		return user;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public boolean onlyOpen() {
		return onlyOpen;
	}

	public boolean filtersByBookCopy() {
		return !UUIDHelper.isDefaultValue(bookCopy);
	}

	public boolean filtersByUser() {
		return !UUIDHelper.isDefaultValue(user);
	}

	public boolean filtersByDueDate() {
		return !DateHelper.isDefaultValue(dueDate);
	}

}
