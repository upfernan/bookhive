package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.loan.returnbook.ReturnBookDomain;
import co.com.bookhive.circulation.domain.loan.returnbook.ReturnedLoanDomain;
import co.com.bookhive.circulation.services.input.dto.request.ReturnBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReturnBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ReturnBookInputPort;
import co.com.bookhive.circulation.services.usecase.loan.returnbook.ReturnBookUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class ReturnBookInputAdapter implements ReturnBookInputPort {

	private final ReturnBookUseCase useCase;

	public ReturnBookInputAdapter(final ReturnBookUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ReturnBookResponseDTO execute(final ReturnBookRequestDTO request) {
		if (ObjectHelper.isNull(request)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATA_REQUIRED,
					"El adaptador de ReturnBook recibió una petición nula.");
		}

		final ReturnedLoanDomain returned = useCase.execute(new ReturnBookDomain(
				request.bookCopy(),
				UUIDHelper.getDefaultValue(),
				UUIDHelper.getDefaultValue(),
				DateHelper.getDefaultValue()));

		if (ObjectHelper.isNull(returned)) {
			throw BookHiveException.create(
					MessageKey.LOAN_RETURN_NOT_CONFIRMED,
					"El caso de uso ReturnBook devolvió un dominio nulo.");
		}

		return new ReturnBookResponseDTO(
				returned.loan(),
				returned.bookCopy(),
				returned.dueDate(),
				returned.returnDate(),
				returned.overdueDays(),
				returned.hasFine(),
				returned.fine(),
				returned.dailyRate(),
				returned.fineAmount());
	}

}
