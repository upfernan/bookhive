package co.com.bookhive.access.services.output.ports.repositories;

import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;

public interface PersonRepository {

	PersonEntity findActive(String identity);

}
