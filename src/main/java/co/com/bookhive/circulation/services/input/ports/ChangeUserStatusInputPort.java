package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.UserLifecycleRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserLifecycleResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ChangeUserStatusInputPort extends InputPortWithReturn<UserLifecycleRequestDTO, UserLifecycleResponseDTOs.Account> {

}
