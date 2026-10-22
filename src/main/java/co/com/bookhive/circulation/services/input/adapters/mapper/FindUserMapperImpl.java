package co.com.bookhive.circulation.services.input.adapters.mapper;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindUserMapperImpl implements FindUserMapper {

	@Override
	public FindUserDomain toDomain(final FindUserRequestDTO request) {

		final FindUserRequestDTO safeRequest = ObjectHelper.getDefaultValue(request, new FindUserRequestDTO(""));

		return new FindUserDomain(safeRequest.email());
	}

	@Override
	public FindUserResponseDTO toResponse(final FoundUserDomain domain) {

		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindUser devolvió un resultado nulo.");
		}

		return new FindUserResponseDTO(
				domain.id(),
				domain.name(),
				domain.email(),
				domain.activeLoans(),
				domain.maxLoans(),
				domain.pendingFines(),
				domain.blocks());
	}

}
