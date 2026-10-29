package co.com.bookhive.payment.services.usecase.finepayment.confirmfinepayment.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.PaymentStatus;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.ConfirmFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.FinePaymentStatusDomain;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;
import co.com.bookhive.payment.testdata.FakePayments;

class ConfirmFinePaymentUseCaseImplTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID PAYER = UUID.randomUUID();
	private static final String REFERENCE = "uco-" + UUID.randomUUID();

	private FakePayments.Payments payments;
	private FakePayments.Gateway gateway;
	private List<PaymentTransactionDomain> applied;

	@BeforeEach
	void prepare() {
		payments = new FakePayments.Payments().with(FakePayments.payment(REFERENCE, FINE, PAYER, 300000L));
		gateway = new FakePayments.Gateway();
		applied = new ArrayList<>();
	}

	@Test
	@DisplayName("pregunta a la pasarela el estado real y lo registra antes de responder")
	void confirmsWithGateway() {
		gateway.with(new GatewayTransactionEntity("t-1", REFERENCE, PaymentStatus.APPROVED, 300000L));

		final FinePaymentStatusDomain status = useCase(PAYER).execute(new ConfirmFinePaymentDomain(REFERENCE, "t-1"));

		assertEquals(PaymentStatus.APPROVED, status.status());
		assertTrue(status.finePaid());
		assertEquals(300000L, status.amountInCents());
		assertEquals(PaymentTransactionDomain.SOURCE_QUERY, applied.get(0).source());
		assertEquals("t-1", applied.get(0).transaction());
	}

	@Test
	@DisplayName("el pago de otra persona no se encuentra")
	void rejectsPaymentOfAnotherPerson() {
		final ConfirmFinePaymentUseCaseImpl useCase = useCase(UUID.randomUUID());
		final ConfirmFinePaymentDomain request = new ConfirmFinePaymentDomain(REFERENCE, "t-1");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.PAYMENT_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("una transacción de otro pago o que la pasarela no conoce se rechaza")
	void rejectsTransactionOfAnotherPayment() {
		gateway.with(new GatewayTransactionEntity("t-1", "uco-otro", PaymentStatus.APPROVED, 300000L));
		final ConfirmFinePaymentUseCaseImpl useCase = useCase(PAYER);
		final ConfirmFinePaymentDomain foreign = new ConfirmFinePaymentDomain(REFERENCE, "t-1");
		final ConfirmFinePaymentDomain unknown = new ConfirmFinePaymentDomain(REFERENCE, "t-2");

		assertEquals(MessageKey.PAYMENT_TRANSACTION_MISMATCH,
				assertThrows(BookHiveException.class, () -> useCase.execute(foreign)).getUserMessageKey());
		assertEquals(MessageKey.PAYMENT_TRANSACTION_MISMATCH,
				assertThrows(BookHiveException.class, () -> useCase.execute(unknown)).getUserMessageKey());
		assertTrue(applied.isEmpty());
	}

	@Test
	@DisplayName("si la pasarela no responde se informa que el pago en línea no está disponible")
	void reportsGatewayDown() {
		gateway.down();
		final ConfirmFinePaymentUseCaseImpl useCase = useCase(PAYER);
		final ConfirmFinePaymentDomain request = new ConfirmFinePaymentDomain(REFERENCE, "t-1");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(BookHiveException.Kind.UNAVAILABLE, exception.getKind());
	}

	@Test
	@DisplayName("sin referencia o con una transacción inválida se piden los datos")
	void validatesRequest() {
		final ConfirmFinePaymentUseCaseImpl useCase = useCase(PAYER);
		final ConfirmFinePaymentDomain invalid = new ConfirmFinePaymentDomain(REFERENCE, "t/../1");

		assertEquals(MessageKey.PAYMENT_REFERENCE_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());
		assertEquals(MessageKey.PAYMENT_TRANSACTION_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(invalid)).getUserMessageKey());
	}

	@Test
	@DisplayName("no se crea sin sus dependencias")
	void requiresDependencies() {
		assertThrows(BookHiveException.class, () -> new ConfirmFinePaymentUseCaseImpl(payments, gateway, null, null));
	}

	private ConfirmFinePaymentUseCaseImpl useCase(final UUID person) {
		return new ConfirmFinePaymentUseCaseImpl(payments, gateway,
				new FakePayments.Session(UUID.randomUUID(), person, "uco"),
				transaction -> {
					applied.add(transaction);
					return new AppliedPaymentTransactionDomain(true, transaction.reference(), transaction.status(),
							transaction.amountInCents(), PaymentStatus.isApproved(transaction.status()));
				});
	}

	@Test
	@DisplayName("una transacción rechazada no deja la multa pagada")
	void declinedIsNotPaid() {
		gateway.with(new GatewayTransactionEntity("t-1", REFERENCE, PaymentStatus.DECLINED, 300000L));

		assertFalse(useCase(PAYER).execute(new ConfirmFinePaymentDomain(REFERENCE, "t-1")).finePaid());
	}

}
