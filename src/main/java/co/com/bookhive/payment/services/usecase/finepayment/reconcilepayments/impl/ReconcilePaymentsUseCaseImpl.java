package co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments.impl;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconcilePaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconciledPaymentsDomain;
import co.com.bookhive.payment.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.TenantScopePort;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.ApplyPaymentTransactionUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments.ReconcilePaymentsUseCase;

public final class ReconcilePaymentsUseCaseImpl implements ReconcilePaymentsUseCase {

	private final InstitutionDirectoryPort institutions;
	private final TenantScopePort tenantScope;
	private final FinePaymentRepository paymentRepository;
	private final PaymentGatewayPort gateway;
	private final ApplyPaymentTransactionUseCase applyTransaction;
	private final Clock clock;

	public ReconcilePaymentsUseCaseImpl(
			final InstitutionDirectoryPort institutions,
			final TenantScopePort tenantScope,
			final FinePaymentRepository paymentRepository,
			final PaymentGatewayPort gateway,
			final ApplyPaymentTransactionUseCase applyTransaction,
			final Clock clock) {

		super();
		this.institutions = requireDependency(institutions, "el directorio de instituciones");
		this.tenantScope = requireDependency(tenantScope, "el cambio de institución");
		this.paymentRepository = requireDependency(paymentRepository, "el repositorio de pagos de multas");
		this.gateway = requireDependency(gateway, "la pasarela de pagos");
		this.applyTransaction = requireDependency(applyTransaction, "el registro de transacciones");
		this.clock = requireDependency(clock, "el reloj");
	}

	@Override
	public ReconciledPaymentsDomain execute(final ReconcilePaymentsDomain domain) {
		final ReconcilePaymentsDomain received = ObjectHelper.getDefaultValue(domain, new ReconcilePaymentsDomain(null));
		final ReconcilePaymentsDomain request = DateHelper.isDefaultValue(received.now())
				? new ReconcilePaymentsDomain(clock.instant())
				: received;
		final List<String> schemas = institutions.activeSchemas();

		int reviewed = 0;
		int finesPaid = 0;
		for (final String schema : schemas) {
			final ReconciledPaymentsDomain result = ObjectHelper.getDefaultValue(
					tenantScope.within(schema, () -> reconcile(request)),
					new ReconciledPaymentsDomain(0, 0, 0));
			reviewed += result.reviewed();
			finesPaid += result.finesPaid();
		}
		return new ReconciledPaymentsDomain(schemas.size(), reviewed, finesPaid);
	}

	private ReconciledPaymentsDomain reconcile(final ReconcilePaymentsDomain request) {
		final List<FinePaymentEntity> unresolved = paymentRepository.findByCriteria(
				FinePaymentCriteria.unresolvedCreatedBetween(request.createdFrom(), request.createdUntil()));

		int reviewed = 0;
		int finesPaid = 0;
		for (final FinePaymentEntity payment : unresolved) {
			final Optional<List<GatewayTransactionEntity>> transactions = transactionsOf(payment);
			if (transactions.isEmpty()) {
				break;
			}
			reviewed++;
			finesPaid += (int) transactions.get().stream()
					.filter(transaction -> payment.reference().equals(transaction.reference()))
					.filter(transaction -> applyTransaction.execute(transactionOf(payment, transaction)).finePaid())
					.count();
		}
		return new ReconciledPaymentsDomain(1, reviewed, finesPaid);
	}

	private Optional<List<GatewayTransactionEntity>> transactionsOf(final FinePaymentEntity payment) {
		try {
			return Optional.of(gateway.findTransactionsOf(payment.reference(), payment.createdAt()));
		} catch (final BookHiveException exception) {
			if (BookHiveException.Kind.UNAVAILABLE.equals(exception.getKind())) {
				return Optional.empty();
			}
			throw exception;
		}
	}

	private static PaymentTransactionDomain transactionOf(
			final FinePaymentEntity payment, final GatewayTransactionEntity transaction) {

		return new PaymentTransactionDomain(
				payment.reference(),
				transaction.id(),
				transaction.status(),
				transaction.amountInCents(),
				PaymentTransactionDomain.SOURCE_RECONCILIATION);
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "El caso de uso ReconcilePayments se creó sin " + name + ".");
		}
		return dependency;
	}

}
