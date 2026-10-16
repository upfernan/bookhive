package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;

public interface FineRepository
		extends FindByCriteriaRepository<FineEntity, FineCriteria> {

}
