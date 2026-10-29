package co.com.bookhive.payment.services.usecase.finepayment.applypaymenttransaction.impl;

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
import co.com.bookhive.payment.domain.finepayment.PaymentStatus;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.AppliedPaymentTransactionDomain;
import co.com.bookhive.payment.domain.finepayment.applypaymenttransaction.PaymentTransactionDomain;
import co.com.bookhive.payment.testdata.FakePayments;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class ApplyPaymentTransactionUseCaseImplTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID PAYER = UUID.randomUUID();
	private static final String REFERENCE = "uco-" + UUID.randomUUID();
	private static final long CENTS = 300000L;

	private FakePayments.Payments payments;
	private FakePayments.Events events;
	private FakePayments.Fines fines;
	private List<String> audited;

	@BeforeEach
	void prepare() {
		payments = new FakePayments.Payments().with(FakePayments.payment(REFERENCE, FINE, PAYER, CENTS));
		events = new FakePayments.Events();
		fines = new FakePayments.Fines();
		audited = new ArrayList<>();
	}

	@Test
	@DisplayName("una transacción aprobada por el valor congelado queda registrada y salda la multa")
	void approvedTransactionSettlesFine() {
		final AppliedPaymentTransactionDomain applied = useCase().execute(transaction("t-1", PaymentStatus.APPROVED, CENTS));

		assertTrue(applied.known());
		assertTrue(applied.finePaid());
		assertEquals(PaymentStatus.APPROVED, applied.status());
		assertEquals(1, events.recorded().size());
		assertEquals(List.of(FINE + " " + REFERENCE + " 3000.00"), fines.settlements());
		assertEquals(List.of(AuditPort.FINE_PAYMENT_UPDATED, AuditPort.FINE_PAID), audited);
	}

	@Test
	@DisplayName("el mismo aviso repetido no se registra dos veces ni vuelve a auditar")
	void repeatedTransactionIsAppliedOnce() {
		final ApplyPaymentTransactionUseCaseImpl useCase = useCase();
		useCase.execute(transaction("t-1", PaymentStatus.APPROVED, CENTS));
		audited.clear();

		final AppliedPaymentTransactionDomain applied = useCase.execute(transaction("t-1", PaymentStatus.APPROVED, CENTS));

		assertTrue(applied.finePaid());
		assertEquals(1, events.recorded().size());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("una transacción rechazada queda registrada y la multa sigue pendiente")
	void declinedTransactionKeepsFinePending() {
		final AppliedPaymentTransactionDomain applied = useCase().execute(transaction("t-1", PaymentStatus.DECLINED, CENTS));

		assertFalse(applied.finePaid());
		assertEquals(1, events.recorded().size());
		assertTrue(fines.settlements().isEmpty());
	}

	@Test
	@DisplayName("una aprobación por un valor distinto al mostrado no salda la multa y se audita")
	void differentAmountIsInconsistent() {
		final AppliedPaymentTransactionDomain applied = useCase().execute(transaction("t-1", PaymentStatus.APPROVED, 100L));

		assertFalse(applied.finePaid());
		assertTrue(fines.settlements().isEmpty());
		assertEquals(List.of(AuditPort.FINE_PAYMENT_UPDATED, AuditPort.FINE_PAYMENT_INCONSISTENT), audited);
	}

	@Test
	@DisplayName("un segundo pago aprobado de una multa ya saldada se audita para reversarlo")
	void secondPaymentIsDuplicated() {
		fines.settledBy("uco-otro");

		final AppliedPaymentTransactionDomain applied = useCase().execute(transaction("t-2", PaymentStatus.APPROVED, CENTS));

		assertTrue(applied.finePaid());
		assertEquals(List.of(AuditPort.FINE_PAYMENT_UPDATED, AuditPort.FINE_PAYMENT_DUPLICATED), audited);
	}

	@Test
	@DisplayName("una referencia que no es de un pago de BookHive no se registra")
	void unknownReferenceIsIgnored() {
		final AppliedPaymentTransactionDomain applied = useCase().execute(new PaymentTransactionDomain(
				"uco-" + UUID.randomUUID(), "t-1", PaymentStatus.APPROVED, CENTS, PaymentTransactionDomain.SOURCE_EVENT));

		assertFalse(applied.known());
		assertTrue(events.recorded().isEmpty());
	}

	@Test
	@DisplayName("una transacción sin estado conocido no se registra")
	void incompleteTransactionIsIgnored() {
		assertFalse(useCase().execute(transaction("t-1", "RARO", CENTS)).known());
		assertFalse(useCase().execute(null).known());
		assertTrue(events.recorded().isEmpty());
	}

	@Test
	@DisplayName("no se crea sin sus dependencias")
	void requiresDependencies() {
		assertThrows(BookHiveException.class, () -> new ApplyPaymentTransactionUseCaseImpl(payments, events, fines, null));
	}

	private ApplyPaymentTransactionUseCaseImpl useCase() {
		return new ApplyPaymentTransactionUseCaseImpl(payments, events, fines,
				(action, institution, person, detail) -> audited.add(action));
	}

	private static PaymentTransactionDomain transaction(final String id, final String status, final long cents) {
		return new PaymentTransactionDomain(REFERENCE, id, status, cents, PaymentTransactionDomain.SOURCE_EVENT);
	}

}
