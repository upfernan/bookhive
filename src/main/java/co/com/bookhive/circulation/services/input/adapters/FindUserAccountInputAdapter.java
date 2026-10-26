package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.account.finduseraccount.UserAccountDomain;
import co.com.bookhive.circulation.services.input.dto.response.UserAccountResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserAccountInputPort;
import co.com.bookhive.circulation.services.usecase.account.finduseraccount.FindUserAccountUseCase;

public final class FindUserAccountInputAdapter implements FindUserAccountInputPort {

	private final FindUserAccountUseCase useCase;

	public FindUserAccountInputAdapter(final FindUserAccountUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public UserAccountResponseDTO execute() {
		final UserAccountDomain account = useCase.execute();
		return new UserAccountResponseDTO(
				account.activeLoans().stream().map(loan -> new UserAccountResponseDTO.Loan(
						loan.loan(), loan.book(), loan.loanDate(), loan.dueDate(), loan.overdueDays(), loan.accruedFine())).toList(),
				account.pendingFines().stream().map(fine -> new UserAccountResponseDTO.Fine(
						fine.fine(), fine.book(), fine.issueDate(), fine.overdueDays(), fine.dailyRate(), fine.amount())).toList(),
				account.pendingFinesTotal(),
				account.accruingFinesTotal(),
				account.returnedLoans().stream().map(loan -> new UserAccountResponseDTO.Return(
						loan.loan(), loan.book(), loan.loanDate(), loan.dueDate(), loan.returnDate(), loan.overdueDays())).toList(),
				account.payments().stream().map(payment -> new UserAccountResponseDTO.Payment(
						payment.fine(), payment.book(), payment.reference(), payment.amount(), payment.paidAt())).toList());
	}

}
