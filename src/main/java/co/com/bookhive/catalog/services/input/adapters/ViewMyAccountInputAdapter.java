package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.account.viewmyaccount.MyAccountDomain;
import co.com.bookhive.catalog.services.input.dto.response.MyAccountResponseDTO;
import co.com.bookhive.catalog.services.input.ports.ViewMyAccountInputPort;
import co.com.bookhive.catalog.services.usecase.account.viewmyaccount.ViewMyAccountUseCase;

public final class ViewMyAccountInputAdapter implements ViewMyAccountInputPort {

	private final ViewMyAccountUseCase useCase;

	public ViewMyAccountInputAdapter(final ViewMyAccountUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public MyAccountResponseDTO execute() {
		final MyAccountDomain account = useCase.execute();
		return new MyAccountResponseDTO(
				account.activeLoans().stream().map(item -> new MyAccountResponseDTO.Loan(item.loan(), book(item.book()),
						item.loanDate(), item.dueDate(), item.overdueDays(), item.accruedFine())).toList(),
				account.reservations().stream().map(ListMyReservationsInputAdapter::toResponse).toList(),
				account.pendingFines().stream().map(item -> new MyAccountResponseDTO.Fine(item.fine(), book(item.book()),
						item.issueDate(), item.overdueDays(), item.dailyRate(), item.amount())).toList(),
				account.pendingFinesTotal(),
				account.accruingFinesTotal(),
				account.loanHistory().stream().map(item -> new MyAccountResponseDTO.Return(item.loan(), book(item.book()),
						item.loanDate(), item.dueDate(), item.returnDate(), item.overdueDays())).toList(),
				account.payments().stream().map(item -> new MyAccountResponseDTO.Payment(item.fine(), book(item.book()),
						item.reference(), item.amount(), item.paidAt())).toList());
	}

	private static MyAccountResponseDTO.Book book(final MyAccountDomain.Book book) {
		return new MyAccountResponseDTO.Book(book.id(), book.title(), book.authors());
	}

}
