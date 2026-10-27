package co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentJpaEntity;


public interface FinePaymentJpaRepository
		extends JpaRepository<FinePaymentJpaEntity, UUID>,
		JpaSpecificationExecutor<FinePaymentJpaEntity> {

}
