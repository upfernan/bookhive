package co.com.bookhive.circulation.services.input.ports;

import co.com.bookhive.circulation.services.input.dto.request.InstitutionSettingsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.InstitutionSettingsResponseDTO;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ConfigureInstitutionInputPort
		extends InputPortWithReturn<InstitutionSettingsRequestDTO, InstitutionSettingsResponseDTO> {
}
