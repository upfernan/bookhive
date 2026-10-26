package co.com.bookhive.circulation.services.input.adapters;

import co.com.bookhive.circulation.domain.institution.configureinstitution.InstitutionSettingsDomain;
import co.com.bookhive.circulation.services.input.dto.request.InstitutionSettingsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.InstitutionSettingsResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ConfigureInstitutionInputPort;
import co.com.bookhive.circulation.services.input.ports.FindInstitutionSettingsInputPort;
import co.com.bookhive.circulation.services.usecase.institution.configureinstitution.ConfigureInstitutionUseCase;
import co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.FindInstitutionSettingsUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class InstitutionSettingsInputAdapters {

	private InstitutionSettingsInputAdapters() {
		super();
	}

	public static ConfigureInstitutionInputPort configure(final ConfigureInstitutionUseCase useCase) {
		return request -> {
			final InstitutionSettingsRequestDTO safeRequest = ObjectHelper.getDefaultValue(
					request, new InstitutionSettingsRequestDTO(0, 0, null, null));
			return responseOf(useCase.execute(new InstitutionSettingsDomain(
					safeRequest.loanDays(),
					safeRequest.maxConcurrentLoans(),
					safeRequest.dailyFineRate(),
					safeRequest.timeZone())));
		};
	}

	public static FindInstitutionSettingsInputPort find(final FindInstitutionSettingsUseCase useCase) {
		return () -> responseOf(useCase.execute());
	}

	private static InstitutionSettingsResponseDTO responseOf(final InstitutionSettingsDomain settings) {
		return new InstitutionSettingsResponseDTO(
				settings.loanDays(), settings.maxConcurrentLoans(), settings.dailyFineRate(), settings.timeZone());
	}

}
