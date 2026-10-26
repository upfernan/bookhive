package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.fine.settlefine.SettleFineDomain;
import co.com.bookhive.circulation.domain.fine.settlefine.SettledFineDomain;
import co.com.bookhive.circulation.services.input.dto.request.SettleFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.SettleFineResponseDTO;
import co.com.bookhive.circulation.services.input.ports.SettleFineInputPort;
import co.com.bookhive.circulation.services.usecase.fine.settlefine.SettleFineUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class SettleFineInputAdapter implements SettleFineInputPort {

	private final SettleFineUseCase useCase;

	public SettleFineInputAdapter(final SettleFineUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public SettleFineResponseDTO execute(final SettleFineRequestDTO request) {
		final SettleFineRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new SettleFineRequestDTO(null, null, null));
		final SettledFineDomain settled = useCase.execute(
				new SettleFineDomain(safeRequest.fine(), safeRequest.paymentReference(), safeRequest.amount()));
		return new SettleFineResponseDTO(settled.newlySettled(), settled.paymentReference());
	}

}
