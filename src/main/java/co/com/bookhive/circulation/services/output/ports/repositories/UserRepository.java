package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;

public interface UserRepository
		extends FindByCriteriaRepository<UserEntity, UserCriteria> {
}
