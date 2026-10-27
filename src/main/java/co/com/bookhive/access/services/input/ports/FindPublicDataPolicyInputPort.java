package co.com.bookhive.access.services.input.ports;

import co.com.bookhive.access.services.input.dto.response.PublicDataPolicyResponseDTO;

public interface FindPublicDataPolicyInputPort {

	PublicDataPolicyResponseDTO execute(String domain);

}
