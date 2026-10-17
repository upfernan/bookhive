package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.PersonEntity;

public interface PersonRepository {

	PersonEntity findActive(String identity);

}
