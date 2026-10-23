package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class LoanReturnCriteria {

	private final UUID bookCopy;
	private final LocalDate returnDate;

	private LoanReturnCriteria(final UUID bookCopy, final LocalDate returnDate) {
		super();
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		this.returnDate = DateHelper.getDefaultValue(returnDate);
	}

	public static LoanReturnCriteria ofBookCopyOn(final UUID bookCopy, final LocalDate returnDate) {
		return new LoanReturnCriteria(bookCopy, returnDate);
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	public boolean isComplete() {
		return !UUIDHelper.isDefaultValue(bookCopy) && !DateHelper.isDefaultValue(returnDate);
	}

}
