package co.com.bookhive.payment.domain.finepayment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.ConfirmFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.FinePaymentStatusDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconcilePaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconciledPaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.PaymentEventDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.FinePaymentCheckoutDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.StartFinePaymentDomain;

class FinePaymentDomainsTest {

	@Test
	@DisplayName("pedir el pago exige la multa")
	void startRequiresFine() {
		final StartFinePaymentDomain withoutFine = new StartFinePaymentDomain(null);

		assertEquals(MessageKey.FINE_ID_REQUIRED,
				assertThrows(BookHiveException.class, withoutFine::validate).getUserMessageKey());
		assertDoesNotThrow(new StartFinePaymentDomain(UUID.randomUUID())::validate);
	}

	@Test
	@DisplayName("confirmar el pago exige la referencia y una transacción con caracteres seguros")
	void confirmRequiresSafeTransaction() {
		final ConfirmFinePaymentDomain withoutReference = new ConfirmFinePaymentDomain(" ", "t-1");
		final ConfirmFinePaymentDomain unsafe = new ConfirmFinePaymentDomain("uco-1", "t-1?x=1");

		assertEquals(MessageKey.PAYMENT_REFERENCE_REQUIRED,
				assertThrows(BookHiveException.class, withoutReference::validate).getUserMessageKey());
		assertEquals(MessageKey.PAYMENT_TRANSACTION_REQUIRED,
				assertThrows(BookHiveException.class, unsafe::validate).getUserMessageKey());
		assertDoesNotThrow(new ConfirmFinePaymentDomain("uco-1", " 12-1700000000-49201 ")::validate);
	}

	@Test
	@DisplayName("una transacción solo está completa con referencia, número y estado conocido")
	void transactionCompleteness() {
		assertTrue(new PaymentTransactionDomain("uco-1", "t-1", "approved", 10L, "EVENTO").isComplete());
		assertEquals(PaymentStatus.APPROVED, new PaymentTransactionDomain("uco-1", "t-1", "approved", 10L, "").status());
		assertFalse(new PaymentTransactionDomain("uco-1", "", PaymentStatus.APPROVED, 10L, "").isComplete());
		assertFalse(new PaymentTransactionDomain("", "t-1", PaymentStatus.APPROVED, 10L, "").isComplete());
		assertEquals(0L, new PaymentTransactionDomain(null, null, null, -5L, null).amountInCents());
	}

	@Test
	@DisplayName("la revisión mira los pagos de los últimos días que ya tuvieron tiempo de responder")
	void reconciliationWindow() {
		final Instant now = Instant.parse("2026-10-27T06:00:00Z");
		final ReconcilePaymentsDomain request = new ReconcilePaymentsDomain(now);

		assertEquals(now.minus(ReconcilePaymentsDomain.LOOK_BACK), request.createdFrom());
		assertEquals(now.minus(ReconcilePaymentsDomain.WAIT_BEFORE_ASKING), request.createdUntil());
		assertEquals(DateHelper.getDefaultTimestamp(), new ReconcilePaymentsDomain(null).now());
		assertEquals(0, new ReconciledPaymentsDomain(-1, -1, -1).reviewed());
	}

	@Test
	@DisplayName("los resultados nunca quedan con nulos")
	void resultsHaveNoNulls() {
		final FinePaymentCheckoutDomain checkout = new FinePaymentCheckoutDomain(null, null, null, -1L, null, null, null, null);
		final FinePaymentStatusDomain status = new FinePaymentStatusDomain(null, null, -1L, false);
		final AppliedPaymentTransactionDomain unknown = AppliedPaymentTransactionDomain.unknown(null);
		final PaymentEventDomain event = new PaymentEventDomain(null, null, null, -1L, null, -1L, null);

		assertEquals("", checkout.signature());
		assertEquals(0L, checkout.amountInCents());
		assertEquals("", status.status());
		assertFalse(unknown.known());
		assertEquals(List.of(), event.signedValues());
		assertEquals(0L, event.timestamp());
	}

}
