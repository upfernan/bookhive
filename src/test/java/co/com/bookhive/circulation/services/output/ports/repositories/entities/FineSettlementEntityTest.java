package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class FineSettlementEntityTest {

	@Test
	@DisplayName("sin datos nace con valores por defecto, nunca nulos")
	void startsWithDefaults() {
		final FineSettlementEntity settlement = new FineSettlementEntity(null, null, null, null, null);

		assertEquals(UUIDHelper.getDefaultValue(), settlement.id());
		assertEquals(UUIDHelper.getDefaultValue(), settlement.fine());
		assertEquals("", settlement.paymentReference());
		assertEquals(BigDecimal.ZERO, settlement.amount());
		assertEquals(DateHelper.getDefaultTimestamp(), settlement.settledAt());
	}

	@Test
	@DisplayName("guarda los datos del pago con que se saldó la multa")
	void storesPaymentData() {
		final UUID fine = UUID.randomUUID();
		final Instant now = Instant.now();

		final FineSettlementEntity settlement = new FineSettlementEntity(fine, fine, " uco-1 ", new BigDecimal("3000"), now);

		assertEquals(fine, settlement.fine());
		assertEquals("uco-1", settlement.paymentReference());
		assertEquals(new BigDecimal("3000"), settlement.amount());
		assertEquals(now, settlement.settledAt());
	}

}
