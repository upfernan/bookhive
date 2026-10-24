package co.com.bookhive.catalog.services.input.adapters;

import java.util.List;

import co.com.bookhive.catalog.services.input.dto.response.MyReservationResponseDTO;
import co.com.bookhive.catalog.services.input.ports.ListMyReservationsInputPort;
import co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.ListMyReservationsUseCase;

public final class ListMyReservationsInputAdapter implements ListMyReservationsInputPort {

	private final ListMyReservationsUseCase useCase;

	public ListMyReservationsInputAdapter(final ListMyReservationsUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public List<MyReservationResponseDTO> execute() {
		return useCase.execute().stream()
				.map(reservation -> new MyReservationResponseDTO(
						reservation.reservation(),
						reservation.book(),
						reservation.title(),
						reservation.authors(),
						reservation.status(),
						reservation.position(),
						reservation.requestedAt(),
						reservation.pickupDeadline()))
				.toList();
	}

}
