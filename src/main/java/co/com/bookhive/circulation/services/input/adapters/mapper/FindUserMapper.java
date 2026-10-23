package co.com.bookhive.circulation.services.input.adapters.mapper;

import co.com.bookhive.circulation.domain.user.finduser.FindUserDomain;
import co.com.bookhive.circulation.domain.user.finduser.FoundUserDomain;
import co.com.bookhive.circulation.services.input.dto.request.FindUserRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindUserResponseDTO;

public interface FindUserMapper {

	FindUserDomain toDomain(FindUserRequestDTO request);

	FindUserResponseDTO toResponse(FoundUserDomain domain);

}
