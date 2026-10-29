package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.UserJpaEntity;

public interface UserJpaRepository
		extends JpaRepository<UserJpaEntity, UUID>,
		JpaSpecificationExecutor<UserJpaEntity> {
}
