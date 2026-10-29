package co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.impl;

import java.util.Optional;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.PaymentAmount;
import co.com.bookhive.payment.domain.finepayment.PaymentStatus;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.services.output.ports.FinesPort;
import co.com.bookhive.payment.services.output.ports.entities.SettledFineEntity;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentEventRepository;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEventEntity;
import co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.ApplyPaymentTransactionUseCase;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ApplyPaymentTransactionUseCaseImpl implements ApplyPaymentTransactionUseCase {

	private final FinePaymentRepository paymentRepository;
	private final FinePaymentEventRepository eventRepository;
	private final FinesPort fines;
	private final AuditPort audit;

	public ApplyPaymentTransactionUseCaseImpl(
			final FinePaymentRepository paymentRepository,
			final FinePaymentEventRepository eventRepository,
			final FinesPort fines,
			final AuditPort audit) {

		super();
		this.paymentRepository = requireDependency(paymentRepository, "el repositorio de pagos de multas");
		this.eventRepository = requireDependency(eventRepository, "el repositorio de pasos de los pagos");
		this.fines = requireDependency(fines, "las multas de circulación");
		this.audit = requireDependency(audit, "la auditoría");
	}

	@Override
	public AppliedPaymentTransactionDomain execute(final PaymentTransactionDomain domain) {
		final PaymentTransactionDomain transaction =
				ObjectHelper.getDefaultValue(domain, new PaymentTransactionDomain(null, null, null, 0L, null));

		if (!transaction.isComplete()) {
			return AppliedPaymentTransactionDomain.unknown(transaction.reference());
		}

		final Optional<FinePaymentEntity> found = paymentRepository
				.findByCriteria(FinePaymentCriteria.withReference(transaction.reference()))
				.stream()
				.findFirst();
		if (found.isEmpty()) {
			return AppliedPaymentTransactionDomain.unknown(transaction.reference());
		}

		final FinePaymentEntity payment = found.get();
		final boolean recorded = eventRepository.register(eventOf(payment, transaction));
		if (recorded) {
			audit.register(AuditPort.FINE_PAYMENT_UPDATED, payment.institution(), payment.payer(),
					payment.reference() + " transaccion " + transaction.transaction() + " estado "
							+ transaction.status() + " origen " + transaction.source());
		}

		final boolean finePaid = PaymentStatus.isApproved(transaction.status())
				&& settle(payment, transaction, recorded);

		return new AppliedPaymentTransactionDomain(
				true, payment.reference(), transaction.status(), transaction.amountInCents(), finePaid);
	}

	private boolean settle(
			final FinePaymentEntity payment,
			final PaymentTransactionDomain transaction,
			final boolean firstTime) {

		if (transaction.amountInCents() != payment.amountInCents()) {
			if (firstTime) {
				audit.register(AuditPort.FINE_PAYMENT_INCONSISTENT, payment.institution(), payment.payer(),
						payment.reference() + " aprobado por " + transaction.amountInCents()
								+ " centavos y se esperaban " + payment.amountInCents());
			}
			return false;
		}

		final SettledFineEntity settled = fines.settle(
				payment.fine(), payment.reference(), PaymentAmount.fromCents(payment.amountInCents()));

		if (settled.newlySettled()) {
			audit.register(AuditPort.FINE_PAID, payment.institution(), payment.payer(),
					"multa " + payment.fine() + " pago " + payment.reference());
		} else if (firstTime && !payment.reference().equals(settled.paymentReference())) {
			audit.register(AuditPort.FINE_PAYMENT_DUPLICATED, payment.institution(), payment.payer(),
					"multa " + payment.fine() + " ya saldada con " + settled.paymentReference()
							+ "; el pago " + payment.reference() + " debe reversarse");
		}
		return true;
	}

	private static FinePaymentEventEntity eventOf(
			final FinePaymentEntity payment, final PaymentTransactionDomain transaction) {

		return new FinePaymentEventEntity(
				UUIDHelper.generateNew(),
				payment.id(),
				transaction.transaction(),
				transaction.status(),
				transaction.amountInCents(),
				transaction.source(),
				null);
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "El caso de uso ApplyPaymentTransaction se creó sin " + name + ".");
		}
		return dependency;
	}

}
