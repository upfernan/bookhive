package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanReturnCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
import co.com.bookhive.shared.services.output.ports.repositories.CreateRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface LoanReturnRepository
		extends CreateRepository<LoanReturnEntity>,
		FindByCriteriaRepository<LoanReturnEntity, LoanReturnCriteria> {
}
