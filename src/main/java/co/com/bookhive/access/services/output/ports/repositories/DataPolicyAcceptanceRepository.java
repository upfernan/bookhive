package co.com.bookhive.access.services.output.ports.repositories;

import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity;
import co.com.bookhive.shared.services.output.ports.repositories.CreateRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface DataPolicyAcceptanceRepository
		extends CreateRepository<DataPolicyAcceptanceEntity>,
		FindByCriteriaRepository<DataPolicyAcceptanceEntity, DataPolicyAcceptanceCriteria> {

}
