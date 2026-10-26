package co.com.bookhive.payment.services.usecase.finepayment.startfinepayment.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.PaymentReference;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.FinePaymentCheckoutDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.StartFinePaymentDomain;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.testdata.FakePayments;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class StartFinePaymentUseCaseImplTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID PAYER = UUID.randomUUID();
	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-10-26T12:10:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	private FakePayments.Fines fines;
	private FakePayments.Payments payments;
	private FakePayments.Gateway gateway;
	private List<String> audited;

	@BeforeEach
	void prepare() {
		fines = new FakePayments.Fines().payable(FINE, new BigDecimal("3000.00"));
		payments = new FakePayments.Payments();
		gateway = new FakePayments.Gateway();
		audited = new ArrayList<>();
	}

	@Test
	@DisplayName("congela el valor de la multa en un pago nuevo con su referencia y lo firma para la pasarela")
	void startsPaymentWithFrozenAmount() {
		final FinePaymentCheckoutDomain checkout = useCase("uco").execute(new StartFinePaymentDomain(FINE));

		final FinePaymentEntity created = payments.created().get(0);
		assertEquals(300000L, created.getAmountInCents());
		assertEquals("COP", created.getCurrency());
		assertEquals(FINE, created.getFine());
		assertEquals(PAYER, created.getPayer());
		assertEquals(INSTITUTION, created.getInstitution());
		assertEquals(NOW.plus(StartFinePaymentUseCaseImpl.VALID_FOR), created.getExpiresAt());
		assertEquals(new PaymentReference("uco", created.getId()).value(), created.getReference());
		assertEquals(created.getReference(), checkout.reference());
		assertEquals(300000L, checkout.amountInCents());
		assertEquals("COP", checkout.currency());
		assertEquals("firma-" + created.getReference(), checkout.signature());
		assertEquals("pub_test_1", checkout.publicKey());
		assertEquals(List.of(AuditPort.FINE_PAYMENT_STARTED), audited);
	}

	@Test
	@DisplayName("un doble clic reutiliza el pago abierto en vez de crear otro")
	void reusesOpenPayment() {
		final FinePaymentEntity open = FakePayments.payment("uco-abierto", FINE, PAYER, 300000L);
		open.setExpiresAt(NOW.plusSeconds(1200));
		payments.with(open);

		final FinePaymentCheckoutDomain checkout = useCase("uco").execute(new StartFinePaymentDomain(FINE));

		assertEquals("uco-abierto", checkout.reference());
		assertTrue(payments.created().isEmpty());
		assertTrue(audited.isEmpty());
		assertTrue(payments.searches().get(0).onlyUnresolved());
	}

	@Test
	@DisplayName("un pago abierto a punto de vencer no se reutiliza")
	void doesNotReusePaymentAboutToExpire() {
		final FinePaymentEntity open = FakePayments.payment("uco-abierto", FINE, PAYER, 300000L);
		open.setExpiresAt(NOW.plusSeconds(60));
		payments.with(open);

		useCase("uco").execute(new StartFinePaymentDomain(FINE));

		assertEquals(1, payments.created().size());
	}

	@Test
	@DisplayName("una multa ya saldada no se vuelve a cobrar")
	void rejectsPaidFine() {
		fines.alreadyPaid(FINE, new BigDecimal("3000"));
		final StartFinePaymentUseCaseImpl useCase = useCase("uco");
		final StartFinePaymentDomain request = new StartFinePaymentDomain(FINE);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.FINE_ALREADY_PAID, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("un valor que no se puede cobrar en centavos se rechaza")
	void rejectsAmountThatCannotBeCharged() {
		fines.payable(FINE, new BigDecimal("10.005"));
		final StartFinePaymentUseCaseImpl useCase = useCase("uco");
		final StartFinePaymentDomain request = new StartFinePaymentDomain(FINE);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.PAYMENT_AMOUNT_INVALID, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("sin multa se pide indicar cuál y sin sesión es un error inesperado")
	void requiresFineAndSession() {
		final StartFinePaymentUseCaseImpl withSchema = useCase("uco");
		final StartFinePaymentUseCaseImpl withoutSchema = useCase("");
		final StartFinePaymentDomain request = new StartFinePaymentDomain(FINE);

		assertEquals(MessageKey.FINE_ID_REQUIRED,
				assertThrows(BookHiveException.class, () -> withSchema.execute(null)).getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> withoutSchema.execute(request)).getKind());
	}

	@Test
	@DisplayName("no se crea sin reloj")
	void requiresClock() {
		final FakePayments.Session session = new FakePayments.Session(INSTITUTION, PAYER, "uco");
		final AuditPort audit = (action, institution, person, detail) -> audited.add(action);

		assertThrows(BookHiveException.class,
				() -> new StartFinePaymentUseCaseImpl(fines, payments, gateway, session, audit, null));
	}

	private StartFinePaymentUseCaseImpl useCase(final String schema) {
		return new StartFinePaymentUseCaseImpl(fines, payments, gateway,
				new FakePayments.Session(INSTITUTION, PAYER, schema),
				(action, institution, person, detail) -> audited.add(action),
				CLOCK);
	}

}
