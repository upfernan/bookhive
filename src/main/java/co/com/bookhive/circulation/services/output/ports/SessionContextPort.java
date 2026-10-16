package co.com.bookhive.circulation.services.output.ports;

import java.util.UUID;

public interface SessionContextPort {

	UUID getInstitution();

	UUID getLibrarian();

}
