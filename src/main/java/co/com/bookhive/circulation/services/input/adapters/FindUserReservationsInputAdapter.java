package co.com.bookhive.circulation.services.input.adapters;

import java.util.List;
import co.com.bookhive.circulation.services.input.dto.response.UserReservationResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserReservationsInputPort;
import co.com.bookhive.circulation.services.usecase.reservation.finduserreservations.FindUserReservationsUseCase;

public final class FindUserReservationsInputAdapter implements FindUserReservationsInputPort {

	private final FindUserReservationsUseCase useCase;

	public FindUserReservationsInputAdapter(final FindUserReservationsUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public List<UserReservationResponseDTO> execute() {
		return useCase.execute().stream()
				.map(reservation -> new UserReservationResponseDTO(
						reservation.reservation(),
						reservation.book(),
						reservation.status(),
						reservation.position(),
						reservation.requestedAt(),
						reservation.pickupDeadline()))
				.toList();
	}

}
