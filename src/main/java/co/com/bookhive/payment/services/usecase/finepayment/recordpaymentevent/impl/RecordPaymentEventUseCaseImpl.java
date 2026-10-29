package co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent.impl;

import java.util.Optional;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.PaymentReference;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.PaymentEventDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.RecordedPaymentEventDomain;
import co.com.bookhive.payment.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.TenantScopePort;
import co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.ApplyPaymentTransactionUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent.RecordPaymentEventUseCase;

public final class RecordPaymentEventUseCaseImpl implements RecordPaymentEventUseCase {

	private final PaymentGatewayPort gateway;
	private final InstitutionDirectoryPort institutions;
	private final TenantScopePort tenantScope;
	private final ApplyPaymentTransactionUseCase applyTransaction;

	public RecordPaymentEventUseCaseImpl(
			final PaymentGatewayPort gateway,
			final InstitutionDirectoryPort institutions,
			final TenantScopePort tenantScope,
			final ApplyPaymentTransactionUseCase applyTransaction) {

		super();
		this.gateway = requireDependency(gateway, "la pasarela de pagos");
		this.institutions = requireDependency(institutions, "el directorio de instituciones");
		this.tenantScope = requireDependency(tenantScope, "el cambio de institución");
		this.applyTransaction = requireDependency(applyTransaction, "el registro de transacciones");
	}

	@Override
	public RecordedPaymentEventDomain execute(final PaymentEventDomain domain) {
		final PaymentEventDomain event =
				ObjectHelper.getDefaultValue(domain, new PaymentEventDomain(null, null, null, 0L, null, 0L, null));

		if (!gateway.isAuthentic(event.signedValues(), event.timestamp(), event.checksum())) {
			throw BookHiveException.createNotAllowed(
					MessageKey.PAYMENT_EVENT_NOT_AUTHENTIC,
					"Llegó un aviso de pago de " + event.reference() + " con una firma que no coincide.");
		}

		final Optional<PaymentReference> reference = PaymentReference.parse(event.reference())
				.filter(parsed -> institutions.activeSchemas().contains(parsed.schema()));
		if (reference.isEmpty()) {
			return new RecordedPaymentEventDomain(false);
		}

		final AppliedPaymentTransactionDomain applied = tenantScope.within(
				reference.get().schema(),
				() -> applyTransaction.execute(new PaymentTransactionDomain(
						event.reference(),
						event.transaction(),
						event.status(),
						event.amountInCents(),
						PaymentTransactionDomain.SOURCE_EVENT)));

		return new RecordedPaymentEventDomain(
				ObjectHelper.getDefaultValue(applied, AppliedPaymentTransactionDomain.unknown(event.reference())).known());
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "El caso de uso RecordPaymentEvent se creó sin " + name + ".");
		}
		return dependency;
	}

}
