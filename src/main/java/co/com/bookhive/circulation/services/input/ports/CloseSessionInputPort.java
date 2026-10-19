package co.com.bookhive.circulation.services.input.ports;

public interface CloseSessionInputPort {

	void execute(String sessionToken);

}
