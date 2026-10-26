package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineSettlementCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineSettlementEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface FineSettlementRepository
		extends FindByCriteriaRepository<FineSettlementEntity, FineSettlementCriteria> {

	boolean settle(FineSettlementEntity settlement);

}
