package co.com.bookhive.payment.services.usecase.finepayment.reconcilepayments.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.payment.domain.finepayment.PaymentStatus;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconcilePaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconciledPaymentsDomain;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.testdata.FakePayments;

class ReconcilePaymentsUseCaseImplTest {

	private static final Instant NOW = Instant.parse("2026-10-27T06:00:00Z");
	private static final String REFERENCE = "uco-" + UUID.randomUUID();

	private FakePayments.Payments payments;
	private FakePayments.Gateway gateway;
	private FakePayments.Tenants tenants;
	private List<PaymentTransactionDomain> applied;

	@BeforeEach
	void prepare() {
		payments = new FakePayments.Payments()
				.with(FakePayments.payment(REFERENCE, UUID.randomUUID(), UUID.randomUUID(), 300000L));
		gateway = new FakePayments.Gateway();
		tenants = new FakePayments.Tenants();
		applied = new ArrayList<>();
	}

	@Test
	@DisplayName("revisa en cada institución los pagos sin respuesta y aplica lo que diga la pasarela")
	void reconcilesUnresolvedPayments() {
		gateway.with(new GatewayTransactionEntity("t-1", REFERENCE, PaymentStatus.APPROVED, 300000L))
				.with(new GatewayTransactionEntity("t-9", "uco-otro", PaymentStatus.APPROVED, 300000L));

		final ReconciledPaymentsDomain result = useCase().execute(null);

		assertEquals(2, result.institutions());
		assertEquals(2, result.reviewed());
		assertEquals(2, result.finesPaid());
		assertEquals(List.of("uco", "eafit"), tenants.visited());
		assertEquals(PaymentTransactionDomain.SOURCE_RECONCILIATION, applied.get(0).source());
		final FinePaymentCriteria search = payments.searches().get(0);
		assertTrue(search.onlyUnresolved());
		assertEquals(NOW.minus(ReconcilePaymentsDomain.LOOK_BACK), search.getCreatedFrom());
		assertEquals(NOW.minus(ReconcilePaymentsDomain.WAIT_BEFORE_ASKING), search.getCreatedUntil());
	}

	@Test
	@DisplayName("si la pasarela no responde deja la revisión para la próxima vez sin fallar")
	void stopsWhenGatewayIsDown() {
		gateway.down();

		final ReconciledPaymentsDomain result = useCase().execute(new ReconcilePaymentsDomain(NOW));

		assertEquals(0, result.reviewed());
		assertTrue(applied.isEmpty());
	}

	@Test
	@DisplayName("no se crea sin reloj")
	void requiresClock() {
		assertThrows(BookHiveException.class,
				() -> new ReconcilePaymentsUseCaseImpl(List::of, tenants, payments, gateway, null, null));
	}

	private ReconcilePaymentsUseCaseImpl useCase() {
		return new ReconcilePaymentsUseCaseImpl(() -> List.of("uco", "eafit"), tenants, payments, gateway,
				transaction -> {
					applied.add(transaction);
					return new AppliedPaymentTransactionDomain(true, transaction.reference(), transaction.status(),
							transaction.amountInCents(), true);
				},
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

}
