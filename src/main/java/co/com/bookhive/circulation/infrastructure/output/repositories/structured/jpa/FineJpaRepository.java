package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineJpaEntity;

@Repository
public interface FineJpaRepository
		extends JpaRepository<FineJpaEntity, UUID>,
		JpaSpecificationExecutor<FineJpaEntity> {

}
