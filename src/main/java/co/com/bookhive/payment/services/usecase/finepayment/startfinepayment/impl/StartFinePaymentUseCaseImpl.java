package co.com.bookhive.payment.services.usecase.finepayment.startfinepayment.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.PaymentAmount;
import co.com.bookhive.payment.domain.finepayment.PaymentReference;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.FinePaymentCheckoutDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.StartFinePaymentDomain;
import co.com.bookhive.payment.services.output.ports.FinesPort;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.SessionContextPort;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutEntity;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutRequestEntity;
import co.com.bookhive.payment.services.output.ports.entities.PayableFineEntity;
import co.com.bookhive.payment.services.output.ports.repositories.FinePaymentRepository;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.usecase.finepayment.startfinepayment.StartFinePaymentUseCase;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class StartFinePaymentUseCaseImpl implements StartFinePaymentUseCase {

	static final Duration VALID_FOR = Duration.ofMinutes(30);
	static final Duration MINIMUM_TIME_LEFT = Duration.ofMinutes(5);

	private final FinesPort fines;
	private final FinePaymentRepository paymentRepository;
	private final PaymentGatewayPort gateway;
	private final SessionContextPort session;
	private final AuditPort audit;
	private final Clock clock;

	public StartFinePaymentUseCaseImpl(
			final FinesPort fines,
			final FinePaymentRepository paymentRepository,
			final PaymentGatewayPort gateway,
			final SessionContextPort session,
			final AuditPort audit,
			final Clock clock) {

		super();
		this.fines = requireDependency(fines, "las multas de circulación");
		this.paymentRepository = requireDependency(paymentRepository, "el repositorio de pagos de multas");
		this.gateway = requireDependency(gateway, "la pasarela de pagos");
		this.session = requireDependency(session, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
		this.clock = requireDependency(clock, "el reloj");
	}

	@Override
	public FinePaymentCheckoutDomain execute(final StartFinePaymentDomain domain) {
		final StartFinePaymentDomain request = ObjectHelper.getDefaultValue(domain, new StartFinePaymentDomain(null));
		request.validate();

		final UUID payer = session.getPerson();
		final UUID institution = session.getInstitution();
		final String schema = session.getSchema();
		if (UUIDHelper.isDefaultValue(payer) || TextHelper.isDefaultValue(schema)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "Se pidió pagar la multa " + request.fine() + " sin una sesión resuelta.");
		}

		final PayableFineEntity fine = fines.find(request.fine(), payer);
		if (!fine.pending()) {
			throw BookHiveException.createConflict(
					MessageKey.FINE_ALREADY_PAID, "La multa " + request.fine() + " ya está saldada.");
		}

		final long amountInCents = amountInCentsOf(fine);
		final Instant now = clock.instant();

		final FinePaymentEntity payment = paymentRepository
				.findByCriteria(FinePaymentCriteria.openFor(fine.fine(), payer, now.plus(MINIMUM_TIME_LEFT)))
				.stream()
				.filter(open -> open.amountInCents() == amountInCents)
				.max(Comparator.comparing(FinePaymentEntity::createdAt))
				.orElseGet(() -> startPayment(fine, payer, institution, schema, amountInCents, now));

		final CheckoutEntity checkout = gateway.checkoutFor(new CheckoutRequestEntity(
				payment.reference(), payment.amountInCents(), payment.currency(), payment.expiresAt()));

		return new FinePaymentCheckoutDomain(
				checkout.checkoutUrl(),
				checkout.publicKey(),
				payment.currency(),
				payment.amountInCents(),
				payment.reference(),
				checkout.signature(),
				checkout.expirationTime(),
				checkout.redirectUrl());
	}

	private FinePaymentEntity startPayment(
			final PayableFineEntity fine,
			final UUID payer,
			final UUID institution,
			final String schema,
			final long amountInCents,
			final Instant now) {

		final UUID id = UUIDHelper.generateNew(clock);
		final FinePaymentEntity payment = new FinePaymentEntity(
				id,
				new PaymentReference(schema, id).value(),
				fine.fine(),
				payer,
				institution,
				amountInCents,
				PaymentAmount.CURRENCY,
				now.plus(VALID_FOR),
				now);
		paymentRepository.create(payment);

		audit.register(AuditPort.FINE_PAYMENT_STARTED, institution, payer,
				payment.reference() + " multa " + fine.fine() + " por " + amountInCents + " centavos");
		return payment;
	}

	private static long amountInCentsOf(final PayableFineEntity fine) {
		if (!PaymentAmount.isPayable(fine.amount())) {
			throw BookHiveException.createConflict(
					MessageKey.PAYMENT_AMOUNT_INVALID, "La multa " + fine.fine() + " vale " + fine.amount() + ".");
		}
		return PaymentAmount.inCents(fine.amount());
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.PAYMENT_UNEXPECTED, "El caso de uso StartFinePayment se creó sin " + name + ".");
		}
		return dependency;
	}

}
