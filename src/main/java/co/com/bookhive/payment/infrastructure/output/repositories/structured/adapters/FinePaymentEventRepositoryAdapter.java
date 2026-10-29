package co.com.bookhive.payment.infrastructure.output.repositories.structured.adapters;

import org.springframework.stereotype.Component;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.FinePaymentEventJpaRepository;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentEventRepository;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEventEntity;

@Component
public class FinePaymentEventRepositoryAdapter implements FinePaymentEventRepository {

	private final FinePaymentEventJpaRepository repository;
	public FinePaymentEventRepositoryAdapter(final FinePaymentEventJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public boolean register(final FinePaymentEventEntity event) {
		if (ObjectHelper.isNull(event)
				|| UUIDHelper.isDefaultValue(event.id())
				|| UUIDHelper.isDefaultValue(event.payment())
				|| TextHelper.isDefaultValue(event.transaction())
				|| TextHelper.isDefaultValue(event.status())) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED,
					"Se intentó guardar un paso de pago sin identificador, pago, transacción o estado.");
		}
		return repository.register(
				event.id(),
				event.payment(),
				event.transaction(),
				event.status(),
				event.amountInCents(),
				event.source()) == 1;
	}

}
