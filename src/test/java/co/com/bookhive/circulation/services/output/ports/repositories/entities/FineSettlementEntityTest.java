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
	@DisplayName("nace con valores por defecto, nunca nulos")
	void startsWithDefaults() {
		final FineSettlementEntity settlement = new FineSettlementEntity();

		assertEquals(UUIDHelper.getDefaultValue(), settlement.getId());
		assertEquals(UUIDHelper.getDefaultValue(), settlement.getFine());
		assertEquals("", settlement.getPaymentReference());
		assertEquals(BigDecimal.ZERO, settlement.getAmount());
		assertEquals(DateHelper.getDefaultTimestamp(), settlement.getSettledAt());
	}

	@Test
	@DisplayName("guarda los datos del pago y convierte los nulos en valores por defecto")
	void storesPaymentData() {
		final FineSettlementEntity settlement = new FineSettlementEntity();
		final UUID fine = UUID.randomUUID();
		final Instant now = Instant.now();

		settlement.setFine(fine);
		settlement.setPaymentReference(" uco-1 ");
		settlement.setAmount(new BigDecimal("3000"));
		settlement.setSettledAt(now);

		assertEquals(fine, settlement.getFine());
		assertEquals("uco-1", settlement.getPaymentReference());
		assertEquals(new BigDecimal("3000"), settlement.getAmount());
		assertEquals(now, settlement.getSettledAt());

		settlement.setPaymentReference(null);
		settlement.setAmount(null);
		assertEquals("", settlement.getPaymentReference());
		assertEquals(BigDecimal.ZERO, settlement.getAmount());
	}

}
