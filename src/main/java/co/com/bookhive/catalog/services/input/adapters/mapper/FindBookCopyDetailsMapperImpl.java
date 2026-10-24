package co.com.bookhive.catalog.services.input.adapters.mapper;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindBookCopyDetailsMapperImpl implements FindBookCopyDetailsMapper {

	@Override
	public FindBookCopyDetailsDomain toDomain(final FindBookCopyDetailsRequestDTO request) {

		final FindBookCopyDetailsRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new FindBookCopyDetailsRequestDTO(""));

		return new FindBookCopyDetailsDomain(safeRequest.barcode(), safeRequest.user());
	}

	@Override
	public FindBookCopyDetailsResponseDTO toResponse(final BookCopyDetailsDomain domain) {

		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso FindBookCopyDetails devolvió un resultado nulo.");
		}

		return new FindBookCopyDetailsResponseDTO(
				domain.id(),
				domain.barcode(),
				domain.title(),
				domain.authors(),
				domain.branch(),
				domain.available(),
				domain.lendable(),
				domain.blocks());
	}

}
