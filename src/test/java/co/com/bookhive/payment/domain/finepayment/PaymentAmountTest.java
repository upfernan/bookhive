package co.com.bookhive.payment.domain.finepayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentAmountTest {

	@Test
	@DisplayName("convierte el valor de la multa a centavos y de vuelta sin perder nada")
	void convertsToCentsAndBack() {
		assertEquals(300000L, PaymentAmount.inCents(new BigDecimal("3000")));
		assertEquals(150050L, PaymentAmount.inCents(new BigDecimal("1500.50")));
		assertEquals(new BigDecimal("3000.00"), PaymentAmount.fromCents(300000L));
		assertEquals(0L, PaymentAmount.inCents(null));
	}

	@Test
	@DisplayName("solo se cobra un valor positivo con máximo dos decimales")
	void onlyPositiveAmountsWithCentsArePayable() {
		assertTrue(PaymentAmount.isPayable(new BigDecimal("3000.00")));
		assertTrue(PaymentAmount.isPayable(new BigDecimal("3000.000")));
		assertFalse(PaymentAmount.isPayable(new BigDecimal("10.005")));
		assertFalse(PaymentAmount.isPayable(BigDecimal.ZERO));
		assertFalse(PaymentAmount.isPayable(new BigDecimal("-1")));
		assertFalse(PaymentAmount.isPayable(null));
		assertFalse(PaymentAmount.isPayable(new BigDecimal("1E+30")));
	}

	@Test
	@DisplayName("reconoce los estados de la pasarela y cuáles son definitivos")
	void knowsGatewayStatuses() {
		assertTrue(PaymentStatus.isKnown(" approved "));
		assertTrue(PaymentStatus.isApproved("APPROVED"));
		assertTrue(PaymentStatus.isFinal(PaymentStatus.DECLINED));
		assertFalse(PaymentStatus.isFinal(PaymentStatus.PENDING));
		assertFalse(PaymentStatus.isKnown("RARO"));
		assertEquals("", PaymentStatus.normalize(null));
	}

}
