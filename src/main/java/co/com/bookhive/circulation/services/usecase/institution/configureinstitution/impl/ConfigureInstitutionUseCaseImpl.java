package co.com.bookhive.circulation.services.usecase.institution.configureinstitution.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.institution.configureinstitution.InstitutionSettingsDomain;
import co.com.bookhive.circulation.domain.institution.configureinstitution.ParameterChange;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParameterChangeRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.usecase.institution.configureinstitution.ConfigureInstitutionUseCase;
import co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.impl.FindInstitutionSettingsUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ConfigureInstitutionUseCaseImpl implements ConfigureInstitutionUseCase {

	private final InstitutionParametersRepository institutionParametersRepository;
	private final InstitutionParameterChangeRepository changeRepository;
	private final SessionContextPort sessionContext;
	private final AuditPort audit;

	public ConfigureInstitutionUseCaseImpl(
			final InstitutionParametersRepository institutionParametersRepository,
			final InstitutionParameterChangeRepository changeRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit) {

		super();
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.changeRepository = requireDependency(changeRepository, "el historial de cambios de parámetros");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
	}

	@Override
	public InstitutionSettingsDomain execute(final InstitutionSettingsDomain domain) {
		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.create(
					MessageKey.INSTITUTION_SETTINGS_DATA_REQUIRED, "Se pidió configurar la institución sin datos.");
		}
		domain.validate();

		final InstitutionParametersEntity parameters = CurrentInstitution.parameters(institutionParametersRepository);
		final InstitutionSettingsDomain current = FindInstitutionSettingsUseCaseImpl.settingsOf(parameters);
		final List<ParameterChange> changes = ParameterChange.between(current, domain);
		if (changes.isEmpty()) {
			return current;
		}

		parameters.setLoanDays(domain.loanDays());
		parameters.setMaxConcurrentLoans(domain.maxConcurrentLoans());
		parameters.setDailyFineRate(domain.normalizedDailyFineRate());
		parameters.setTimeZone(domain.timeZone());
		institutionParametersRepository.update(parameters);

		final UUID administrator = sessionContext.getLibrarian();
		changes.forEach(change -> changeRepository.create(new InstitutionParameterChangeEntity(
				UUIDHelper.generateNew(), change.parameter(), change.previousValue(), change.newValue(), administrator, null)));

		audit.register(AuditPort.INSTITUTION_CONFIGURED, sessionContext.getInstitution(), administrator,
				changes.stream().map(ParameterChange::describe).collect(Collectors.joining(", ")));

		return FindInstitutionSettingsUseCaseImpl.settingsOf(parameters);
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.INSTITUTION_WITHOUT_PARAMETERS, "El caso de uso ConfigureInstitution se creó sin " + name + ".");
		}
		return dependency;
	}

}
