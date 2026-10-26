package co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment.impl;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.ConfirmFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.FinePaymentStatusDomain;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.SessionContextPort;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.ApplyPaymentTransactionUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment.ConfirmFinePaymentUseCase;

public final class ConfirmFinePaymentUseCaseImpl implements ConfirmFinePaymentUseCase {

	private final FinePaymentRepository paymentRepository;
	private final PaymentGatewayPort gateway;
	private final SessionContextPort session;
	private final ApplyPaymentTransactionUseCase applyTransaction;

	public ConfirmFinePaymentUseCaseImpl(
			final FinePaymentRepository paymentRepository,
			final PaymentGatewayPort gateway,
			final SessionContextPort session,
			final ApplyPaymentTransactionUseCase applyTransaction) {

		super();
		this.paymentRepository = requireDependency(paymentRepository, "el repositorio de pagos de multas");
		this.gateway = requireDependency(gateway, "la pasarela de pagos");
		this.session = requireDependency(session, "el contexto de la sesión");
		this.applyTransaction = requireDependency(applyTransaction, "el registro de transacciones");
	}

	@Override
	public FinePaymentStatusDomain execute(final ConfirmFinePaymentDomain domain) {
		final ConfirmFinePaymentDomain request =
				ObjectHelper.getDefaultValue(domain, new ConfirmFinePaymentDomain(null, null));
		request.validate();

		final FinePaymentEntity payment = paymentRepository
				.findByCriteria(FinePaymentCriteria.withReference(request.reference()))
				.stream()
				.filter(found -> found.payer().equals(session.getPerson()))
				.findFirst()
				.orElseThrow(() -> BookHiveException.create(
						MessageKey.PAYMENT_NOT_FOUND,
						"El pago " + request.reference() + " no existe o no es de la persona en sesión."));

		final GatewayTransactionEntity transaction = gateway.findTransaction(request.transaction())
				.filter(found -> payment.reference().equals(found.reference()))
				.orElseThrow(() -> BookHiveException.createConflict(
						MessageKey.PAYMENT_TRANSACTION_MISMATCH,
						"La transacción " + request.transaction() + " no es del pago " + payment.reference() + "."));

		final AppliedPaymentTransactionDomain applied = applyTransaction.execute(new PaymentTransactionDomain(
				payment.reference(),
				transaction.id(),
				transaction.status(),
				transaction.amountInCents(),
				PaymentTransactionDomain.SOURCE_QUERY));

		return new FinePaymentStatusDomain(
				payment.reference(), applied.status(), payment.amountInCents(), applied.finePaid());
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "El caso de uso ConfirmFinePayment se creó sin " + name + ".");
		}
		return dependency;
	}

}
