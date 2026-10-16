package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;

public interface LoanJpaRepository
		extends JpaRepository<LoanJpaEntity, UUID>,
		JpaSpecificationExecutor<LoanJpaEntity> {

}
