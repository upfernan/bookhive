package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulation.services.input.adapters.CloseSessionInteractor;
import co.com.bookhive.circulation.services.input.adapters.OpenSessionInteractor;
import co.com.bookhive.circulation.services.input.ports.CloseSessionInputPort;
import co.com.bookhive.circulation.services.input.ports.OpenSessionInputPort;
import co.com.bookhive.circulation.services.output.ports.IdentityProviderPort;
import co.com.bookhive.circulation.services.output.ports.repositories.PersonRepository;

@Configuration
public class SessionConfiguration {

	@Bean
	OpenSessionInputPort openSessionInputPort(
			final IdentityProviderPort identityProvider,
			final PersonRepository personRepository) {

		return new OpenSessionInteractor(identityProvider, personRepository);
	}

	@Bean
	CloseSessionInputPort closeSessionInputPort(final IdentityProviderPort identityProvider) {
		return new CloseSessionInteractor(identityProvider);
	}

}
