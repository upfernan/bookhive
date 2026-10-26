package co.com.bookhive.payment.services.output.ports.repositories;

import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.shared.services.output.ports.repositories.CreateRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface FinePaymentRepository
		extends CreateRepository<FinePaymentEntity>,
		FindByCriteriaRepository<FinePaymentEntity, FinePaymentCriteria> {
}
