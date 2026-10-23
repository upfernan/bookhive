package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.loan.reminddueloans.RemindDueLoansDomain;
import co.com.bookhive.circulation.services.input.dto.request.RemindDueLoansRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.RemindDueLoansResponseDTO;
import co.com.bookhive.circulation.services.input.ports.RemindDueLoansInputPort;
import co.com.bookhive.circulation.services.usecase.loan.reminddueloans.RemindDueLoansUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class RemindDueLoansInputAdapter implements RemindDueLoansInputPort {

	private final RemindDueLoansUseCase useCase;

	public RemindDueLoansInputAdapter(final RemindDueLoansUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public RemindDueLoansResponseDTO execute(final RemindDueLoansRequestDTO request) {
		final RemindDueLoansRequestDTO safeRequest = ObjectHelper.getDefaultValue(request, new RemindDueLoansRequestDTO(null));
		return new RemindDueLoansResponseDTO(useCase.execute(new RemindDueLoansDomain(safeRequest.dueOn(), 0)).reminded());
	}

}
