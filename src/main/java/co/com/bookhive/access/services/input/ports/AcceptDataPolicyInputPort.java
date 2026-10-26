package co.com.bookhive.access.services.input.ports;

import co.com.bookhive.access.services.input.dto.request.AcceptDataPolicyRequestDTO;
import co.com.bookhive.access.services.input.dto.response.DataPolicyResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface AcceptDataPolicyInputPort extends InputPortWithReturn<AcceptDataPolicyRequestDTO, DataPolicyResponseDTO> {

}
