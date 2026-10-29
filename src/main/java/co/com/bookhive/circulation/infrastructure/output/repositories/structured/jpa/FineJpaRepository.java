package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineJpaEntity;

public interface FineJpaRepository
		extends JpaRepository<FineJpaEntity, UUID>,
		JpaSpecificationExecutor<FineJpaEntity> {

}
