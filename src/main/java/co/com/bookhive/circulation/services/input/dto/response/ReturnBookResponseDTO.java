package co.com.bookhive.circulation.services.input.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ReturnBookResponseDTO(
		UUID loan,
		UUID bookCopy,
		LocalDate dueDate,
		LocalDate returnDate,
		int overdueDays,
		boolean fineGenerated,
		UUID fine,
		BigDecimal dailyRate,
		BigDecimal fineAmount,
		boolean reserved,
		String reservedFor) {

	public ReturnBookResponseDTO {
		loan = UUIDHelper.getDefaultValue(loan);
		bookCopy = UUIDHelper.getDefaultValue(bookCopy);
		dueDate = DateHelper.getDefaultValue(dueDate);
		returnDate = DateHelper.getDefaultValue(returnDate);
		overdueDays = Math.max(overdueDays, 0);
		fine = UUIDHelper.getDefaultValue(fine);
		dailyRate = ObjectHelper.getDefaultValue(dailyRate, BigDecimal.ZERO);
		fineAmount = ObjectHelper.getDefaultValue(fineAmount, BigDecimal.ZERO);
		reservedFor = TextHelper.getDefaultValue(reservedFor);
	}

	public ReturnBookResponseDTO(
			final UUID loan,
			final UUID bookCopy,
			final LocalDate dueDate,
			final LocalDate returnDate,
			final int overdueDays,
			final boolean fineGenerated,
			final UUID fine,
			final BigDecimal dailyRate,
			final BigDecimal fineAmount) {

		this(loan, bookCopy, dueDate, returnDate, overdueDays, fineGenerated, fine, dailyRate, fineAmount, false, "");
	}

}
