package co.com.bookhive.circulation.services.input.adapters.mapper;

import java.util.UUID;

import co.com.bookhive.circulation.domain.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class LendBookMapper {

	private LendBookMapper() {
		super();
	}

	public static LendBookDomain toDomain(
			final LendBookRequestDTO request,
			final UUID institution,
			final UUID librarian) {

		if (ObjectHelper.isNull(request)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATA_REQUIRED,
					"El mapeador de LendBook recibió una petición nula.");
		}

		return new LendBookDomain(
				UUIDHelper.getDefaultValue(),
				request.getBookCopy(),
				request.getUser(),
				institution,
				librarian,
				DateHelper.todayInUtc(),
				DateHelper.getDefaultValue());
	}

	public static LendBookResponseDTO toResponse(final LendBookDomain domain) {

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
