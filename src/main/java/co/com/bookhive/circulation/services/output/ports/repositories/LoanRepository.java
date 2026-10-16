package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;

public interface LoanRepository
		extends CreateRepository<LoanEntity>,
		FindRepository<UUID, LoanEntity>,
		FindByCriteriaRepository<LoanEntity, LoanCriteria> {

}
