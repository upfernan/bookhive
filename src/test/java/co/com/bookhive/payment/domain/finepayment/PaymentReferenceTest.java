package co.com.bookhive.payment.domain.finepayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentReferenceTest {

	private static final UUID PAYMENT = UUID.randomUUID();

	@Test
	@DisplayName("la referencia lleva la institución y el pago, y se puede leer de vuelta")
	void roundTrips() {
		final PaymentReference reference = new PaymentReference("uco", PAYMENT);

		assertEquals("uco-" + PAYMENT, reference.value());
		assertEquals(reference, PaymentReference.parse(" uco-" + PAYMENT + " ").orElseThrow());
		assertTrue(reference.isComplete());
	}

	@Test
	@DisplayName("una referencia que no armó BookHive no se reconoce")
	void rejectsForeignReferences() {
		assertTrue(PaymentReference.parse(null).isEmpty());
		assertTrue(PaymentReference.parse("pedido123").isEmpty());
		assertTrue(PaymentReference.parse("-" + PAYMENT).isEmpty());
		assertTrue(PaymentReference.parse("UCO-" + PAYMENT).isEmpty());
		assertTrue(PaymentReference.parse("uco-no-es-un-uuid").isEmpty());
		assertTrue(PaymentReference.parse("uco-00000000-0000-0000-0000-000000000000").isEmpty());
		assertFalse(new PaymentReference(null, null).isComplete());
	}

}
