package co.com.bookhive.shared.services.usecase.message.resolvemessage.impl;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.shared.domain.message.resolvemessage.ResolveMessageDomain;
import co.com.bookhive.shared.services.output.ports.MessageCatalogPort;
import co.com.bookhive.shared.services.usecase.message.resolvemessage.ResolveMessageUseCase;

public final class ResolveMessageUseCaseImpl implements ResolveMessageUseCase {

	private final MessageCatalogPort messageCatalog;

	public ResolveMessageUseCaseImpl(final MessageCatalogPort messageCatalog) {
		super();
		this.messageCatalog = messageCatalog;
	}

	@Override
	public ResolveMessageDomain execute(final ResolveMessageDomain input) {
		final ResolveMessageDomain message = ObjectHelper.getDefaultValue(input, new ResolveMessageDomain("", ""));
		return message.withText(messageCatalog.textOf(message.getKey()));
	}

}
