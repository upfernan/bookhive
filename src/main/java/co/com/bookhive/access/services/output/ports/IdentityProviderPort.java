package co.com.bookhive.access.services.output.ports;

import co.com.bookhive.access.services.output.ports.entities.VerifiedIdentityEntity;

public interface IdentityProviderPort {

	String openSession(String idToken);

	VerifiedIdentityEntity verify(String idToken);

	String identityOf(String sessionToken);

	void closeSession(String sessionToken);

}
