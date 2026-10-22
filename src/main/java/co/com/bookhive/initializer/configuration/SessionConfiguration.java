package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.access.services.input.adapters.CloseSessionInputAdapter;
import co.com.bookhive.access.services.input.adapters.CurrentSessionInputAdapter;
import co.com.bookhive.access.services.input.adapters.OpenSessionInputAdapter;
import co.com.bookhive.access.services.input.ports.CloseSessionInputPort;
import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.access.services.input.ports.OpenSessionInputPort;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.output.ports.SessionContextPort;
import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;

@Configuration
public class SessionConfiguration {

	@Bean
	OpenSessionInputPort openSessionInputPort(
			final IdentityProviderPort identityProvider,
			final PersonRepository personRepository) {

		return new OpenSessionInputAdapter(identityProvider, personRepository);
	}

	@Bean
	CloseSessionInputPort closeSessionInputPort(final IdentityProviderPort identityProvider) {
		return new CloseSessionInputAdapter(identityProvider);
	}

	@Bean
	CurrentSessionInputPort currentSessionInputPort(final SessionContextPort sessionContext) {
		return new CurrentSessionInputAdapter(sessionContext);
	}

}
