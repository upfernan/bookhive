package co.com.bookhive.access.services.usecase.session.closesession.impl;

import co.com.bookhive.access.domain.session.closesession.CloseSessionDomain;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.usecase.session.closesession.CloseSessionUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class CloseSessionUseCaseImpl implements CloseSessionUseCase {

	private final IdentityProviderPort identityProvider;

	public CloseSessionUseCaseImpl(final IdentityProviderPort identityProvider) {
		super();
		this.identityProvider = identityProvider;
	}

	@Override
	public void execute(final CloseSessionDomain input) {

		final CloseSessionDomain session = ObjectHelper.getDefaultValue(input, new CloseSessionDomain(""));

		if (session.hasSession()) {
			identityProvider.closeSession(session.sessionToken());
		}
	}

}
