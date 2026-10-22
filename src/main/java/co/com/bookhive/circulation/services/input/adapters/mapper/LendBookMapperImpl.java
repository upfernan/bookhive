package co.com.bookhive.circulation.services.input.adapters.mapper;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class LendBookMapperImpl implements LendBookMapper {

	@Override
	public LendBookDomain toDomain(final LendBookRequestDTO request) {

		if (ObjectHelper.isNull(request)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATA_REQUIRED,
					"El mapeador de LendBook recibió una petición nula.");
		}

		return new LendBookDomain(
				UUIDHelper.getDefaultValue(),
				request.getBookCopy(),
				request.getUser(),
				UUIDHelper.getDefaultValue(),
				UUIDHelper.getDefaultValue(),
				DateHelper.todayInUtc(),
				DateHelper.getDefaultValue());
	}

	@Override
	public LendBookResponseDTO toResponse(final LendBookDomain domain) {

		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.create(
					MessageKey.LOAN_NOT_CONFIRMED,
					"El caso de uso LendBook devolvió un dominio nulo.");
		}

		return new LendBookResponseDTO(
				domain.getId(),
				domain.getDueDate());
	}

}
