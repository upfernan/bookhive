package co.com.bookhive.access.services.output.ports.repositories;

import co.com.bookhive.access.services.output.ports.repositories.criteria.PersonCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface PersonRepository extends FindByCriteriaRepository<PersonEntity, PersonCriteria> {

}
