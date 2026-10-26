package co.com.bookhive.payment.services.output.ports;

import java.util.UUID;

public interface SessionContextPort {

	UUID getInstitution();

	UUID getPerson();

	String getSchema();

}
