package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.services.input.ports.CloseSessionInputPort;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.crosscutting.helper.TextHelper;

public class CloseSessionInputAdapter implements CloseSessionInputPort {

	private final IdentityProviderPort identityProvider;

	public CloseSessionInputAdapter(final IdentityProviderPort identityProvider) {
		super();
		this.identityProvider = identityProvider;
	}

	@Override
	public void execute(final String sessionToken) {

		if (TextHelper.isDefaultValue(sessionToken)) {
			return;
		}

		identityProvider.closeSession(TextHelper.getDefaultValue(sessionToken));
	}

}
