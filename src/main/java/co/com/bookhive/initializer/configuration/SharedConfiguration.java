package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.com.bookhive.shared.services.input.adapters.ResolveMessageInputAdapter;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;
import co.com.bookhive.shared.services.output.ports.MessageCatalogPort;
import co.com.bookhive.shared.services.usecase.message.resolvemessage.ResolveMessageUseCase;
import co.com.bookhive.shared.services.usecase.message.resolvemessage.impl.ResolveMessageUseCaseImpl;

@Configuration
public class SharedConfiguration {

	@Bean
	ResolveMessageUseCase resolveMessageUseCase(final MessageCatalogPort messageCatalog) {
		return new ResolveMessageUseCaseImpl(messageCatalog);
	}

	@Bean
	ResolveMessageInputPort resolveMessageInputPort(final ResolveMessageUseCase resolveMessageUseCase) {
		return new ResolveMessageInputAdapter(resolveMessageUseCase);
	}

}
