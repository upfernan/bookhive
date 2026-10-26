package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.domain.account.viewmyaccount.MyAccountDomain;
import co.com.bookhive.catalog.domain.account.viewmyaccount.MyAccountDomain.Book;
import co.com.bookhive.catalog.services.output.ports.UserAccountPort;
import co.com.bookhive.circulation.services.input.dto.response.UserAccountResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserAccountInputPort;

@Component
public class CirculationUserAccountAdapter implements UserAccountPort {

	private final FindUserAccountInputPort findUserAccount;

	public CirculationUserAccountAdapter(final FindUserAccountInputPort findUserAccount) {
		super();
		this.findUserAccount = findUserAccount;
	}

	@Override
	public MyAccountDomain findMine() {
		final UserAccountResponseDTO account = findUserAccount.execute();
		return new MyAccountDomain(
				account.activeLoans().stream().map(item -> new MyAccountDomain.Loan(item.loan(), Book.unnamed(item.book()),
						item.loanDate(), item.dueDate(), item.overdueDays(), item.accruedFine())).toList(),
				java.util.List.of(),
				account.pendingFines().stream().map(item -> new MyAccountDomain.Fine(item.fine(), Book.unnamed(item.book()),
						item.issueDate(), item.overdueDays(), item.dailyRate(), item.amount())).toList(),
				account.pendingFinesTotal(),
				account.accruingFinesTotal(),
				account.returnedLoans().stream().map(item -> new MyAccountDomain.Return(item.loan(), Book.unnamed(item.book()),
						item.loanDate(), item.dueDate(), item.returnDate(), item.overdueDays())).toList(),
				account.payments().stream().map(item -> new MyAccountDomain.Payment(item.fine(), Book.unnamed(item.book()),
						item.reference(), item.amount(), item.paidAt())).toList());
	}

}
