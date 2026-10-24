package co.com.bookhive.circulation.services.input.adapters.mapper;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindBookCopyMapperImpl implements FindBookCopyMapper {

	@Override
	public FindBookCopyDomain toDomain(final FindBookCopyRequestDTO request) {

		final FindBookCopyRequestDTO safeRequest = ObjectHelper.getDefaultValue(request, new FindBookCopyRequestDTO(""));

		return new FindBookCopyDomain(safeRequest.barcode(), safeRequest.user());
	}

	@Override
	public FindBookCopyResponseDTO toResponse(final FoundBookCopyDomain domain) {

		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindBookCopy devolvió un resultado nulo.");
		}

		return new FindBookCopyResponseDTO(
				domain.id(),
				domain.book(),
				domain.barcode(),
				domain.available(),
				domain.lendable(),
				domain.blocks());
	}

}
