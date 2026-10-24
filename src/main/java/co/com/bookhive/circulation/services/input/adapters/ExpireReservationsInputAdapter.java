package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.reservation.expirereservations.ExpiredReservationsDomain;
import co.com.bookhive.circulation.services.input.dto.request.ExpireReservationsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ExpiredReservationsResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ExpireReservationsInputPort;
import co.com.bookhive.circulation.services.usecase.reservation.expirereservations.ExpireReservationsUseCase;

public final class ExpireReservationsInputAdapter implements ExpireReservationsInputPort {

	private final ExpireReservationsUseCase useCase;

	public ExpireReservationsInputAdapter(final ExpireReservationsUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ExpiredReservationsResponseDTO execute(final ExpireReservationsRequestDTO request) {
		final ExpiredReservationsDomain result = useCase.execute();
		return new ExpiredReservationsResponseDTO(result.expired(), result.handedOver());
	}

}
