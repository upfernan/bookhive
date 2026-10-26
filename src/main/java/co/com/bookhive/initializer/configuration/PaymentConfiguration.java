package co.com.bookhive.initializer.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.payment.services.input.adapters.ConfirmFinePaymentInputAdapter;
import co.com.bookhive.payment.services.input.adapters.ReconcilePaymentsInputAdapter;
import co.com.bookhive.payment.services.input.adapters.RecordPaymentEventInputAdapter;
import co.com.bookhive.payment.services.input.adapters.StartFinePaymentInputAdapter;
import co.com.bookhive.payment.services.input.ports.ConfirmFinePaymentInputPort;
import co.com.bookhive.payment.services.input.ports.ReconcilePaymentsInputPort;
import co.com.bookhive.payment.services.input.ports.RecordPaymentEventInputPort;
import co.com.bookhive.payment.services.input.ports.StartFinePaymentInputPort;
import co.com.bookhive.payment.services.output.ports.FinesPort;
import co.com.bookhive.payment.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.SessionContextPort;
import co.com.bookhive.payment.services.output.ports.TenantScopePort;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentEventRepository;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.ApplyPaymentTransactionUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.impl.ApplyPaymentTransactionUseCaseImpl;
import co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment.ConfirmFinePaymentUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment.impl.ConfirmFinePaymentUseCaseImpl;
import co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments.ReconcilePaymentsUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments.impl.ReconcilePaymentsUseCaseImpl;
import co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent.RecordPaymentEventUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent.impl.RecordPaymentEventUseCaseImpl;
import co.com.bookhive.payment.services.usecase.finepayment.startfinepayment.StartFinePaymentUseCase;
import co.com.bookhive.payment.services.usecase.finepayment.startfinepayment.impl.StartFinePaymentUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class PaymentConfiguration {

	private static final Clock CLOCK = Clock.systemUTC();

	@Bean
	ApplyPaymentTransactionUseCase applyPaymentTransactionUseCase(
			final FinePaymentRepository finePaymentRepository,
			final FinePaymentEventRepository finePaymentEventRepository,
			final FinesPort fines,
			final AuditPort audit) {

		return new ApplyPaymentTransactionUseCaseImpl(finePaymentRepository, finePaymentEventRepository, fines, audit);
	}

	@Bean
	StartFinePaymentUseCase startFinePaymentUseCase(
			final FinesPort fines,
			final FinePaymentRepository finePaymentRepository,
			final PaymentGatewayPort paymentGateway,
			final SessionContextPort paymentSessionAdapter,
			final AuditPort audit) {

		return new StartFinePaymentUseCaseImpl(
				fines, finePaymentRepository, paymentGateway, paymentSessionAdapter, audit, CLOCK);
	}

	@Bean
	StartFinePaymentInputPort startFinePaymentInputPort(final StartFinePaymentUseCase startFinePaymentUseCase) {
		return new StartFinePaymentInputAdapter(startFinePaymentUseCase);
	}

	@Bean
	ConfirmFinePaymentUseCase confirmFinePaymentUseCase(
			final FinePaymentRepository finePaymentRepository,
			final PaymentGatewayPort paymentGateway,
			final SessionContextPort paymentSessionAdapter,
			final ApplyPaymentTransactionUseCase applyPaymentTransactionUseCase) {

		return new ConfirmFinePaymentUseCaseImpl(
				finePaymentRepository, paymentGateway, paymentSessionAdapter, applyPaymentTransactionUseCase);
	}

	@Bean
	ConfirmFinePaymentInputPort confirmFinePaymentInputPort(final ConfirmFinePaymentUseCase confirmFinePaymentUseCase) {
		return new ConfirmFinePaymentInputAdapter(confirmFinePaymentUseCase);
	}

	@Bean
	RecordPaymentEventUseCase recordPaymentEventUseCase(
			final PaymentGatewayPort paymentGateway,
			final InstitutionDirectoryPort paymentInstitutionDirectoryAdapter,
			final TenantScopePort paymentTenantScopeAdapter,
			final ApplyPaymentTransactionUseCase applyPaymentTransactionUseCase) {

		return new RecordPaymentEventUseCaseImpl(
				paymentGateway,
				paymentInstitutionDirectoryAdapter,
				paymentTenantScopeAdapter,
				applyPaymentTransactionUseCase);
	}

	@Bean
	RecordPaymentEventInputPort recordPaymentEventInputPort(final RecordPaymentEventUseCase recordPaymentEventUseCase) {
		return new RecordPaymentEventInputAdapter(recordPaymentEventUseCase);
	}

	@Bean
	ReconcilePaymentsUseCase reconcilePaymentsUseCase(
			final InstitutionDirectoryPort paymentInstitutionDirectoryAdapter,
			final TenantScopePort paymentTenantScopeAdapter,
			final FinePaymentRepository finePaymentRepository,
			final PaymentGatewayPort paymentGateway,
			final ApplyPaymentTransactionUseCase applyPaymentTransactionUseCase) {

		return new ReconcilePaymentsUseCaseImpl(
				paymentInstitutionDirectoryAdapter,
				paymentTenantScopeAdapter,
				finePaymentRepository,
				paymentGateway,
				applyPaymentTransactionUseCase,
				CLOCK);
	}

	@Bean
	ReconcilePaymentsInputPort reconcilePaymentsInputPort(final ReconcilePaymentsUseCase reconcilePaymentsUseCase) {
		return new ReconcilePaymentsInputAdapter(reconcilePaymentsUseCase);
	}

}
