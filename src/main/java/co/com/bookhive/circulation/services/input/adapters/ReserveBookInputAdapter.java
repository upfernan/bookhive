package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.reservation.reservebook.ReserveBookDomain;
import co.com.bookhive.circulation.domain.reservation.reservebook.ReservedBookDomain;
import co.com.bookhive.circulation.services.input.dto.request.ReserveBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReserveBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ReserveBookInputPort;
import co.com.bookhive.circulation.services.usecase.reservation.reservebook.ReserveBookUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ReserveBookInputAdapter implements ReserveBookInputPort {

	private final ReserveBookUseCase useCase;

	public ReserveBookInputAdapter(final ReserveBookUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ReserveBookResponseDTO execute(final ReserveBookRequestDTO request) {
		if (ObjectHelper.isNull(request)) {
			throw BookHiveException.create(
					MessageKey.RESERVATION_BOOK_REQUIRED,
					"El adaptador de ReserveBook recibió una petición nula.");
		}

		final ReservedBookDomain reserved = useCase.execute(
				new ReserveBookDomain(request.book(), UUIDHelper.getDefaultValue()));

		if (ObjectHelper.isNull(reserved)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso ReserveBook devolvió un dominio nulo.");
		}

		return new ReserveBookResponseDTO(
				reserved.reservation(),
				reserved.book(),
				reserved.status(),
				reserved.position(),
				reserved.pickupDeadline());
	}

}
