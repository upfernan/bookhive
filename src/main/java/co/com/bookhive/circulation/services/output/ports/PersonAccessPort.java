package co.com.bookhive.circulation.services.output.ports;

import java.util.UUID;

public interface PersonAccessPort {

	void deactivate(UUID person);

	void reactivate(UUID person);

	void anonymize(UUID person);

}
