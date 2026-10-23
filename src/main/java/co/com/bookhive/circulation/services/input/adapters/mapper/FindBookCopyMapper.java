package co.com.bookhive.circulation.services.input.adapters.mapper;

import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FindBookCopyDomain;
import co.com.bookhive.circulation.domain.bookcopy.findbookcopy.FoundBookCopyDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;

public interface FindBookCopyMapper {

	FindBookCopyDomain toDomain(FindBookCopyRequestDTO request);

	FindBookCopyResponseDTO toResponse(FoundBookCopyDomain domain);

}
