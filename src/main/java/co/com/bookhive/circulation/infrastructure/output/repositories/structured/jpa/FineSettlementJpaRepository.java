package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineSettlementJpaEntity;

public interface FineSettlementJpaRepository
		extends JpaRepository<FineSettlementJpaEntity, UUID>,
		JpaSpecificationExecutor<FineSettlementJpaEntity> {

	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "INSERT INTO fine_settlement (id, fine_id, payment_reference, amount)"
			+ " VALUES (:id, :fine, :reference, :amount) ON CONFLICT (fine_id) DO NOTHING", nativeQuery = true)
	int settle(
			@Param("id") UUID id,
			@Param("fine") UUID fine,
			@Param("reference") String reference,
			@Param("amount") BigDecimal amount);

}
