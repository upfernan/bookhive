package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.access.services.input.adapters.AcceptDataPolicyInputAdapter;
import co.com.bookhive.access.services.input.adapters.AuthorizeOperationInputAdapter;
import co.com.bookhive.access.services.input.adapters.CloseSessionInputAdapter;
import co.com.bookhive.access.services.input.adapters.CurrentSessionInputAdapter;
import co.com.bookhive.access.services.input.adapters.FindDataPolicyInputAdapter;
import co.com.bookhive.access.services.input.adapters.ListActiveSchemasInputAdapter;
import co.com.bookhive.access.services.input.adapters.OpenSessionInputAdapter;
import co.com.bookhive.access.services.input.adapters.ResolveSchemaInputAdapter;
import co.com.bookhive.access.services.input.ports.AcceptDataPolicyInputPort;
import co.com.bookhive.access.services.input.ports.AuthorizeOperationInputPort;
import co.com.bookhive.access.services.input.ports.CloseSessionInputPort;
import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.access.services.input.ports.FindDataPolicyInputPort;
import co.com.bookhive.access.services.input.ports.ListActiveSchemasInputPort;
import co.com.bookhive.access.services.input.ports.OpenSessionInputPort;
import co.com.bookhive.access.services.input.ports.ResolveSchemaInputPort;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.output.ports.SessionIdentityPort;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyAcceptanceRepository;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyRepository;
import co.com.bookhive.access.services.output.ports.repositories.InstitutionRepository;
import co.com.bookhive.access.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.access.services.usecase.datapolicy.acceptdatapolicy.impl.AcceptDataPolicyUseCaseImpl;
import co.com.bookhive.access.services.usecase.datapolicy.finddatapolicy.impl.FindDataPolicyUseCaseImpl;
import co.com.bookhive.access.services.usecase.institution.listactiveschemas.ListActiveSchemasUseCase;
import co.com.bookhive.access.services.usecase.institution.listactiveschemas.impl.ListActiveSchemasUseCaseImpl;
import co.com.bookhive.access.services.usecase.permission.authorizeoperation.AuthorizeOperationUseCase;
import co.com.bookhive.access.services.usecase.permission.authorizeoperation.impl.AuthorizeOperationUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.closesession.CloseSessionUseCase;
import co.com.bookhive.access.services.usecase.session.closesession.impl.CloseSessionUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.opensession.OpenSessionUseCase;
import co.com.bookhive.access.services.usecase.session.opensession.impl.OpenSessionUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.resolveschema.ResolveSchemaUseCase;
import co.com.bookhive.access.services.usecase.session.resolveschema.impl.ResolveSchemaUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;
import co.com.bookhive.access.services.usecase.session.resolvesession.impl.ResolveSessionUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class SessionConfiguration {

	@Bean
	OpenSessionUseCase openSessionUseCase(
			final IdentityProviderPort identityProvider,
			final PersonRepository personRepository) {

		return new OpenSessionUseCaseImpl(identityProvider, personRepository);
	}

	@Bean
	OpenSessionInputPort openSessionInputPort(final OpenSessionUseCase openSessionUseCase) {
		return new OpenSessionInputAdapter(openSessionUseCase);
	}

	@Bean
	CloseSessionUseCase closeSessionUseCase(final IdentityProviderPort identityProvider) {
		return new CloseSessionUseCaseImpl(identityProvider);
	}

	@Bean
	CloseSessionInputPort closeSessionInputPort(final CloseSessionUseCase closeSessionUseCase) {
		return new CloseSessionInputAdapter(closeSessionUseCase);
	}

	@Bean
	ResolveSessionUseCase resolveSessionUseCase(
			final SessionIdentityPort sessionIdentity,
			final PersonRepository personRepository) {

		return new ResolveSessionUseCaseImpl(sessionIdentity, personRepository);
	}

	@Bean
	CurrentSessionInputPort currentSessionInputPort(final ResolveSessionUseCase resolveSessionUseCase) {
		return new CurrentSessionInputAdapter(resolveSessionUseCase);
	}

	@Bean
	ResolveSchemaUseCase resolveSchemaUseCase(
			final ResolveSessionUseCase resolveSessionUseCase,
			final InstitutionRepository institutionRepository) {

		return new ResolveSchemaUseCaseImpl(resolveSessionUseCase, institutionRepository);
	}

	@Bean
	ResolveSchemaInputPort resolveSchemaInputPort(final ResolveSchemaUseCase resolveSchemaUseCase) {
		return new ResolveSchemaInputAdapter(resolveSchemaUseCase);
	}

	@Bean
	AuthorizeOperationUseCase authorizeOperationUseCase(
			final ResolveSessionUseCase resolveSessionUseCase,
			final PermissionRepository permissionRepository,
			final DataPolicyAcceptanceRepository dataPolicyAcceptanceRepository,
			final AuditPort audit) {

		return new AuthorizeOperationUseCaseImpl(
				resolveSessionUseCase, permissionRepository, dataPolicyAcceptanceRepository, audit);
	}

	@Bean
	FindDataPolicyInputPort findDataPolicyInputPort(
			final ResolveSessionUseCase resolveSessionUseCase,
			final DataPolicyRepository dataPolicyRepository,
			final DataPolicyAcceptanceRepository dataPolicyAcceptanceRepository) {

		return new FindDataPolicyInputAdapter(new FindDataPolicyUseCaseImpl(
				resolveSessionUseCase, dataPolicyRepository, dataPolicyAcceptanceRepository));
	}

	@Bean
	AcceptDataPolicyInputPort acceptDataPolicyInputPort(
			final ResolveSessionUseCase resolveSessionUseCase,
			final DataPolicyRepository dataPolicyRepository,
			final DataPolicyAcceptanceRepository dataPolicyAcceptanceRepository,
			final AuditPort audit) {

		return new AcceptDataPolicyInputAdapter(new AcceptDataPolicyUseCaseImpl(
				resolveSessionUseCase, dataPolicyRepository, dataPolicyAcceptanceRepository, audit));
	}

	@Bean
	AuthorizeOperationInputPort authorizeOperationInputPort(
			final AuthorizeOperationUseCase authorizeOperationUseCase) {

		return new AuthorizeOperationInputAdapter(authorizeOperationUseCase);
	}

	@Bean
	ListActiveSchemasUseCase listActiveSchemasUseCase(final InstitutionRepository institutionRepository) {
		return new ListActiveSchemasUseCaseImpl(institutionRepository);
	}

	@Bean
	ListActiveSchemasInputPort listActiveSchemasInputPort(final ListActiveSchemasUseCase listActiveSchemasUseCase) {
		return new ListActiveSchemasInputAdapter(listActiveSchemasUseCase);
	}

}
