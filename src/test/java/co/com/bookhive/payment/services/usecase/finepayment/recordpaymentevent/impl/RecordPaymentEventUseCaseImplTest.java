package co.com.bookhive.payment.services.usecase.finepayment.recordpaymentevent.impl;

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
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.PaymentEventDomain;
import co.com.bookhive.payment.testdata.FakePayments;

class RecordPaymentEventUseCaseImplTest {

	private static final String REFERENCE = "uco-" + UUID.randomUUID();

	private FakePayments.Gateway gateway;
	private FakePayments.Tenants tenants;
	private List<PaymentTransactionDomain> applied;

	@BeforeEach
	void prepare() {
		gateway = new FakePayments.Gateway();
		tenants = new FakePayments.Tenants();
		applied = new ArrayList<>();
	}

	@Test
	@DisplayName("un aviso auténtico se registra dentro de la institución dueña del pago")
	void recordsEventInItsInstitution() {
		assertTrue(useCase().execute(event(REFERENCE)).applied());

		assertEquals(List.of("uco"), tenants.visited());
		assertEquals(PaymentTransactionDomain.SOURCE_EVENT, applied.get(0).source());
		assertEquals("t-1", applied.get(0).transaction());
		assertEquals(300000L, applied.get(0).amountInCents());
	}

	@Test
	@DisplayName("un aviso con firma inválida se rechaza sin tocar ninguna institución")
	void rejectsForgedEvent() {
		gateway.notAuthentic();
		final RecordPaymentEventUseCaseImpl useCase = useCase();
		final PaymentEventDomain event = event(REFERENCE);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(event));

		assertEquals(MessageKey.PAYMENT_EVENT_NOT_AUTHENTIC, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.NOT_ALLOWED, exception.getKind());
		assertTrue(tenants.visited().isEmpty());
	}

	@Test
	@DisplayName("un aviso de un pago que no es de BookHive o de una institución inactiva se ignora")
	void ignoresForeignOrInactiveReferences() {
		assertFalse(useCase().execute(event("pedido-123")).applied());
		assertFalse(useCase().execute(event("otra-" + UUID.randomUUID())).applied());
		assertFalse(useCase().execute(null).applied());

		assertTrue(tenants.visited().isEmpty());
	}

	@Test
	@DisplayName("no se crea sin sus dependencias")
	void requiresDependencies() {
		assertThrows(BookHiveException.class, () -> new RecordPaymentEventUseCaseImpl(gateway, null, tenants, null));
	}

	private RecordPaymentEventUseCaseImpl useCase() {
		return new RecordPaymentEventUseCaseImpl(gateway, () -> List.of("uco"), tenants, transaction -> {
			applied.add(transaction);
			return new AppliedPaymentTransactionDomain(true, transaction.reference(), transaction.status(),
					transaction.amountInCents(), true);
		});
	}

	private static PaymentEventDomain event(final String reference) {
		return new PaymentEventDomain(reference, "t-1", PaymentStatus.APPROVED, 300000L,
				List.of("t-1", PaymentStatus.APPROVED, "300000"), 1_761_480_000L, "abc");
	}

}
