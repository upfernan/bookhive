package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindRepository;

public interface UserRepository
		extends FindRepository<UUID, UserEntity>,
		FindByCriteriaRepository<UserEntity, UserCriteria> {
}
