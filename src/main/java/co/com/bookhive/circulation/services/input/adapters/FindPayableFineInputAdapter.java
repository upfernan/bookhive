package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.fine.findpayablefine.FindPayableFineDomain;
import co.com.bookhive.circulation.domain.fine.findpayablefine.PayableFineDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindPayableFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.PayableFineResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindPayableFineInputPort;
import co.com.bookhive.circulation.services.usecase.fine.findpayablefine.FindPayableFineUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class FindPayableFineInputAdapter implements FindPayableFineInputPort {

	private final FindPayableFineUseCase useCase;

	public FindPayableFineInputAdapter(final FindPayableFineUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public PayableFineResponseDTO execute(final FindPayableFineRequestDTO request) {
		final FindPayableFineRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new FindPayableFineRequestDTO(null, null));
		final PayableFineDomain fine = useCase.execute(new FindPayableFineDomain(safeRequest.fine(), safeRequest.user()));
		return new PayableFineResponseDTO(fine.fine(), fine.amount(), fine.pending());
	}

}
