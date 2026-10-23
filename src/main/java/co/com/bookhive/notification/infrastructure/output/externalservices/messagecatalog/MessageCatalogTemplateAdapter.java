package co.com.bookhive.notification.infrastructure.output.externalservices.messagecatalog;

import org.springframework.stereotype.Component;

import co.com.bookhive.notification.services.output.ports.MessageTemplatePort;
import co.com.bookhive.shared.services.input.dto.request.ResolveMessageRequestDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;

@Component
public class MessageCatalogTemplateAdapter implements MessageTemplatePort {

	private final ResolveMessageInputPort resolveMessage;

	public MessageCatalogTemplateAdapter(final ResolveMessageInputPort resolveMessage) {
		super();
		this.resolveMessage = resolveMessage;
	}

	@Override
	public String textOf(final String key) {
		return resolveMessage.execute(new ResolveMessageRequestDTO(key)).text();
	}

}
