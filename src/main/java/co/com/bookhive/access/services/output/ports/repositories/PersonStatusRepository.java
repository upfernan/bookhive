package co.com.bookhive.access.services.output.ports.repositories;

import java.util.UUID;

public interface PersonStatusRepository {

	void changeStatus(UUID person, String schema, String status);

	void anonymize(UUID person, String schema);

}
