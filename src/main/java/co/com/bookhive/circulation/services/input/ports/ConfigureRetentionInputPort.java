package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.response.UserLifecycleResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ConfigureRetentionInputPort
		extends InputPortWithReturn<UserLifecycleResponseDTOs.Retention, UserLifecycleResponseDTOs.Retention> {

}
