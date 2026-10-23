package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanReturnJpaEntity;

@Repository
public interface LoanReturnJpaRepository
		extends JpaRepository<LoanReturnJpaEntity, UUID>,
		JpaSpecificationExecutor<LoanReturnJpaEntity> {

}
