package co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentEventJpaEntity;

@Repository
public interface FinePaymentEventJpaRepository
		extends JpaRepository<FinePaymentEventJpaEntity, UUID>,
		JpaSpecificationExecutor<FinePaymentEventJpaEntity> {

	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "INSERT INTO fine_payment_event (id, payment_id, transaction_id, status, amount_in_cents, source)"
			+ " VALUES (:id, :payment, :transaction, :status, :amount, :source)"
			+ " ON CONFLICT (transaction_id, status) DO NOTHING", nativeQuery = true)
	int register(
			@Param("id") UUID id,
			@Param("payment") UUID payment,
			@Param("transaction") String transaction,
			@Param("status") String status,
			@Param("amount") long amountInCents,
			@Param("source") String source);

}
