package co.com.bookhive.payment.services.output.ports.repositories;

import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEventEntity;

public interface FinePaymentEventRepository {

	boolean register(FinePaymentEventEntity event);

}
