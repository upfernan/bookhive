package co.com.bookhive.access.services.output.ports;

public interface IdentityProviderPort {

	String openSession(String idToken);

	String identityOf(String sessionToken);

	void closeSession(String sessionToken);

}
