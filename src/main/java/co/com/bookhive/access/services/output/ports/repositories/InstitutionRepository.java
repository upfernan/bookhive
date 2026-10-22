package co.com.bookhive.access.services.output.ports.repositories;

import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface InstitutionRepository extends FindByCriteriaRepository<InstitutionEntity, InstitutionCriteria> {

}
