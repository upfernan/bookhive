package co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.impl;

import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.institution.configureinstitution.InstitutionSettingsDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.FindInstitutionSettingsUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindInstitutionSettingsUseCaseImpl implements FindInstitutionSettingsUseCase {

	private final InstitutionParametersRepository institutionParametersRepository;

	public FindInstitutionSettingsUseCaseImpl(final InstitutionParametersRepository institutionParametersRepository) {
		super();
		this.institutionParametersRepository = requireRepository(institutionParametersRepository);
	}

	private static InstitutionParametersRepository requireRepository(final InstitutionParametersRepository repository) {
		if (ObjectHelper.isNull(repository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.INSTITUTION_WITHOUT_PARAMETERS,
					"El caso de uso FindInstitutionSettings se creó sin el repositorio de parámetros.");
		}
		return repository;
	}

	@Override
	public InstitutionSettingsDomain execute() {
		return settingsOf(CurrentInstitution.parameters(institutionParametersRepository));
	}

	public static InstitutionSettingsDomain settingsOf(final InstitutionParametersEntity parameters) {
		if (UUIDHelper.isDefaultValue(parameters.getId())) {
			throw BookHiveException.createUnexpected(
					MessageKey.INSTITUTION_WITHOUT_PARAMETERS, "La institución no tiene su fila de parámetros.");
		}
		return new InstitutionSettingsDomain(
				parameters.getLoanDays(),
				parameters.getMaxConcurrentLoans(),
				parameters.getDailyFineRate(),
				parameters.getTimeZone());
	}

}
