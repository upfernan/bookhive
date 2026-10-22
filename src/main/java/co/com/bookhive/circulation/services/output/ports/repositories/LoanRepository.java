package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

import co.com.bookhive.shared.services.output.ports.repositories.FindRepository;

import co.com.bookhive.shared.services.output.ports.repositories.CreateRepository;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;

public interface LoanRepository
		extends CreateRepository<LoanEntity>,
		FindRepository<UUID, LoanEntity>,
		FindByCriteriaRepository<LoanEntity, LoanCriteria> {

}
