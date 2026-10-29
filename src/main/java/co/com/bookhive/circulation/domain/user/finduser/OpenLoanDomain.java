package co.com.bookhive.circulation.domain.user.finduser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record OpenLoanDomain(
		UUID loan,
		UUID bookCopy,
		String barcode,
		UUID book,
		LocalDate loanDate,
		LocalDate dueDate,
		int overdueDays,
		BigDecimal accruedFine) {

	public OpenLoanDomain {
		loan = UUIDHelper.getDefaultValue(loan);
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		barcode = TextHelper.getDefaultValue(barcode);
		book = UUIDHelper.getDefaultValue(book);
		loanDate = DateHelper.getDefaultValue(loanDate);
		dueDate = DateHelper.getDefaultValue(dueDate);
		overdueDays = Math.max(overdueDays, 0);
		accruedFine = ObjectHelper.getDefaultValue(accruedFine, BigDecimal.ZERO);
	}

}
