package co.com.bookhive.circulation.services.input.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class LendBookResponseDTO {

	private UUID loan;
	private LocalDate dueDate;

	public LendBookResponseDTO() {
		super();
		setLoan(UUIDHelper.getDefaultValue());
		setDueDate(DateHelper.getDefaultValue());
	}

	public LendBookResponseDTO(final UUID loan, final LocalDate dueDate) {
		super();
		setLoan(loan);
		setDueDate(dueDate);
	}

	public UUID getLoan() {
		return loan;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	private void setLoan(final UUID loan) {
		this.loan = UUIDHelper.getDefaultValue(loan);
	}

	private void setDueDate(final LocalDate dueDate) {
		this.dueDate = DateHelper.getDefaultValue(dueDate);
	}

}
