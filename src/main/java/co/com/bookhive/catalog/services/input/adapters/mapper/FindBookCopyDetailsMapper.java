package co.com.bookhive.catalog.services.input.adapters.mapper;

import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.BookCopyDetailsDomain;
import co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails.FindBookCopyDetailsDomain;
import co.com.bookhive.catalog.services.input.dto.request.FindBookCopyDetailsRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.FindBookCopyDetailsResponseDTO;

public interface FindBookCopyDetailsMapper {

	FindBookCopyDetailsDomain toDomain(FindBookCopyDetailsRequestDTO request);

	FindBookCopyDetailsResponseDTO toResponse(BookCopyDetailsDomain domain);

}
