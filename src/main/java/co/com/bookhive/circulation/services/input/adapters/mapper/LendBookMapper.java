package co.com.bookhive.circulation.services.input.adapters.mapper;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.input.dto.request.LendBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LendBookResponseDTO;

public interface LendBookMapper {

	LendBookDomain toDomain(LendBookRequestDTO request);

	LendBookResponseDTO toResponse(LendBookDomain domain);

}
