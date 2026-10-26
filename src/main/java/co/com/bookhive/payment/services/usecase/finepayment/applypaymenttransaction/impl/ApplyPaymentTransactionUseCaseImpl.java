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
		final boolean recorded = eventRepository.record(eventOf(payment, transaction));
		if (recorded) {
			audit.record(AuditPort.FINE_PAYMENT_UPDATED, payment.getInstitution(), payment.getPayer(),
					payment.getReference() + " transaccion " + transaction.transaction() + " estado "
							+ transaction.status() + " origen " + transaction.source());
		}

		final boolean finePaid = PaymentStatus.isApproved(transaction.status())
				&& settle(payment, transaction, recorded);

		return new AppliedPaymentTransactionDomain(
				true, payment.getReference(), transaction.status(), transaction.amountInCents(), finePaid);
	}

	private boolean settle(
			final FinePaymentEntity payment,
			final PaymentTransactionDomain transaction,
			final boolean firstTime) {

		if (transaction.amountInCents() != payment.getAmountInCents()) {
			if (firstTime) {
				audit.record(AuditPort.FINE_PAYMENT_INCONSISTENT, payment.getInstitution(), payment.getPayer(),
						payment.getReference() + " aprobado por " + transaction.amountInCents()
								+ " centavos y se esperaban " + payment.getAmountInCents());
			}
			return false;
		}

		final SettledFineEntity settled = fines.settle(
				payment.getFine(), payment.getReference(), PaymentAmount.fromCents(payment.getAmountInCents()));

		if (settled.newlySettled()) {
			audit.record(AuditPort.FINE_PAID, payment.getInstitution(), payment.getPayer(),
					"multa " + payment.getFine() + " pago " + payment.getReference());
		} else if (firstTime && !payment.getReference().equals(settled.paymentReference())) {
			audit.record(AuditPort.FINE_PAYMENT_DUPLICATED, payment.getInstitution(), payment.getPayer(),
					"multa " + payment.getFine() + " ya saldada con " + settled.paymentReference()
							+ "; el pago " + payment.getReference() + " debe reversarse");
		}
		return true;
	}

	private static FinePaymentEventEntity eventOf(
			final FinePaymentEntity payment, final PaymentTransactionDomain transaction) {

		final FinePaymentEventEntity event = new FinePaymentEventEntity();
		event.setId(UUIDHelper.generateNew());
		event.setPayment(payment.getId());
		event.setTransaction(transaction.transaction());
		event.setStatus(transaction.status());
		event.setAmountInCents(transaction.amountInCents());
		event.setSource(transaction.source());
		return event;
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "El caso de uso ApplyPaymentTransaction se creó sin " + name + ".");
		}
		return dependency;
	}

}
