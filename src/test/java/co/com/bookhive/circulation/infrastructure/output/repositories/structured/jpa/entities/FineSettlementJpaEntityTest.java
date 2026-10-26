package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class FineSettlementJpaEntityTest {

	@Test
	@DisplayName("nace con valores por defecto y guarda los datos del pago")
	void storesPaymentData() {
		final FineSettlementJpaEntity settlement = new FineSettlementJpaEntity();
		assertEquals(UUIDHelper.getDefaultValue(), settlement.getFine());
		assertEquals(BigDecimal.ZERO, settlement.getAmount());
		assertEquals(DateHelper.getDefaultTimestamp(), settlement.getSettledAt());

		final UUID id = UUID.randomUUID();
		final Instant now = Instant.now();
		settlement.setId(id);
		settlement.setPaymentReference(" uco-1 ");
		settlement.setAmount(null);
		settlement.setSettledAt(now);

		assertEquals(id, settlement.getId());
		assertEquals("uco-1", settlement.getPaymentReference());
		assertEquals(BigDecimal.ZERO, settlement.getAmount());
		assertEquals(now, settlement.getSettledAt());
	}

}
