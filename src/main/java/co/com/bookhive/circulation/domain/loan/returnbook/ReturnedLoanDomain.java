package co.com.bookhive.circulation.domain.loan.returnbook;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReturnedLoanDomain(
		UUID loan,
		UUID bookCopy,
		LocalDate dueDate,
		LocalDate returnDate,
		int overdueDays,
		UUID fine,
		BigDecimal dailyRate,
		BigDecimal fineAmount) {

	public ReturnedLoanDomain {
		loan = UUIDHelper.getDefaultValue(loan);
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		dueDate = DateHelper.getDefaultValue(dueDate);
		returnDate = DateHelper.getDefaultValue(returnDate);
		overdueDays = Math.max(overdueDays, 0);
		fine = UUIDHelper.getDefaultValue(fine);
		dailyRate = ObjectHelper.getDefaultValue(dailyRate, BigDecimal.ZERO);
		fineAmount = ObjectHelper.getDefaultValue(fineAmount, BigDecimal.ZERO);
	}

	public boolean hasFine() {
		return !UUIDHelper.isDefaultValue(fine);
	}

}
