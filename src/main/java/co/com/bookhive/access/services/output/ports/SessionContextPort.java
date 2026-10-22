package co.com.bookhive.access.services.output.ports;

import java.util.UUID;

public interface SessionContextPort {

	UUID getInstitution();

	UUID getLibrarian();

}
