package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.com.bookhive.circulation.services.input.adapters.InstitutionSettingsInputAdapters;
import co.com.bookhive.circulation.services.input.ports.ConfigureInstitutionInputPort;
import co.com.bookhive.circulation.services.input.ports.FindInstitutionSettingsInputPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParameterChangeRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.usecase.institution.configureinstitution.ConfigureInstitutionUseCase;
import co.com.bookhive.circulation.services.usecase.institution.configureinstitution.impl.ConfigureInstitutionUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.FindInstitutionSettingsUseCase;
import co.com.bookhive.circulation.services.usecase.institution.findinstitutionsettings.impl.FindInstitutionSettingsUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class InstitutionSettingsConfiguration {

	@Bean
	FindInstitutionSettingsUseCase findInstitutionSettingsUseCase(
			final InstitutionParametersRepository institutionParametersRepository) {

		return new FindInstitutionSettingsUseCaseImpl(institutionParametersRepository);
	}

	@Bean
	FindInstitutionSettingsInputPort findInstitutionSettingsInputPort(
			final FindInstitutionSettingsUseCase findInstitutionSettingsUseCase) {

		return InstitutionSettingsInputAdapters.find(findInstitutionSettingsUseCase);
	}

	@Bean
	ConfigureInstitutionUseCase configureInstitutionUseCase(
			final InstitutionParametersRepository institutionParametersRepository,
			final InstitutionParameterChangeRepository institutionParameterChangeRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit) {

		return new ConfigureInstitutionUseCaseImpl(
				institutionParametersRepository, institutionParameterChangeRepository, sessionContext, audit);
	}

	@Bean
	ConfigureInstitutionInputPort configureInstitutionInputPort(final ConfigureInstitutionUseCase configureInstitutionUseCase) {
		return InstitutionSettingsInputAdapters.configure(configureInstitutionUseCase);
	}

}
