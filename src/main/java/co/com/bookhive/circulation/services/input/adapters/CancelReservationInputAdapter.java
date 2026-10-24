package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelReservationDomain;
import co.com.bookhive.circulation.domain.reservation.cancelreservation.CancelledReservationDomain;
import co.com.bookhive.circulation.services.input.dto.request.CancelReservationRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.CancelReservationResponseDTO;
import co.com.bookhive.circulation.services.input.ports.CancelReservationInputPort;
import co.com.bookhive.circulation.services.usecase.reservation.cancelreservation.CancelReservationUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class CancelReservationInputAdapter implements CancelReservationInputPort {

	private final CancelReservationUseCase useCase;

	public CancelReservationInputAdapter(final CancelReservationUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public CancelReservationResponseDTO execute(final CancelReservationRequestDTO request) {
		if (ObjectHelper.isNull(request)) {
			throw BookHiveException.create(
					MessageKey.RESERVATION_ID_REQUIRED,
					"El adaptador de CancelReservation recibió una petición nula.");
		}

		final CancelledReservationDomain cancelled = useCase.execute(
				new CancelReservationDomain(request.reservation(), UUIDHelper.getDefaultValue()));

		if (ObjectHelper.isNull(cancelled)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso CancelReservation devolvió un dominio nulo.");
		}

		return new CancelReservationResponseDTO(cancelled.reservation(), cancelled.book(), cancelled.status());
	}

}
