package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulation.services.input.adapters.UserLifecycleInputAdapters;
import co.com.bookhive.circulation.services.input.ports.ChangeUserStatusInputPort;
import co.com.bookhive.circulation.services.input.ports.ConfigureRetentionInputPort;
import co.com.bookhive.circulation.services.input.ports.CurrentUserInputPort;
import co.com.bookhive.circulation.services.input.ports.ListTaskInstitutionsInputPort;
import co.com.bookhive.circulation.services.input.ports.PurgeExpiredUsersInputPort;
import co.com.bookhive.circulation.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.circulation.services.output.ports.PersonAccessPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParameterChangeRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserLifecycleRepository;
import co.com.bookhive.circulation.services.usecase.account.changeuserstatus.impl.ChangeUserStatusUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.account.configureretention.impl.ConfigureRetentionUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.account.purgeexpiredusers.impl.PurgeExpiredUsersUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class UserLifecycleConfiguration {

	@Bean
	ChangeUserStatusInputPort changeUserStatusInputPort(
			final UserLifecycleRepository users, final PersonAccessPort people, final SessionContextPort session,
			final AuditPort audit) {
		return UserLifecycleInputAdapters.changeStatus(new ChangeUserStatusUseCaseImpl(users, people, session, audit));
	}

	@Bean
	ConfigureRetentionInputPort configureRetentionInputPort(
			final UserLifecycleRepository users, final InstitutionParameterChangeRepository changes,
			final SessionContextPort session, final AuditPort audit) {
		return UserLifecycleInputAdapters.configureRetention(new ConfigureRetentionUseCaseImpl(users, changes, session, audit));
	}

	@Bean
	PurgeExpiredUsersInputPort purgeExpiredUsersInputPort(
			final UserLifecycleRepository users, final PersonAccessPort people, final AuditPort audit) {
		return UserLifecycleInputAdapters.purgeExpired(new PurgeExpiredUsersUseCaseImpl(users, people, audit));
	}

	@Bean
	CurrentUserInputPort currentUserInputPort(final SessionContextPort session) {
		return session::getLibrarian;
	}

	@Bean
	ListTaskInstitutionsInputPort listTaskInstitutionsInputPort(final InstitutionDirectoryPort directory) {
		return directory::activeSchemas;
	}

}
