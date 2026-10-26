package co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEventEntity;

class FinePaymentJpaEntitiesTest {

	private static final UUID ID = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

	@Test
	@DisplayName("el pago guarda todos sus datos y nunca queda con nulos")
	void paymentStoresItsData() {
		final FinePaymentJpaEntity jpa = new FinePaymentJpaEntity();
		final FinePaymentEntity entity = new FinePaymentEntity();
		assertEquals(UUIDHelper.getDefaultValue(), jpa.getPayer());
		assertEquals(DateHelper.getDefaultTimestamp(), entity.getExpiresAt());

		jpa.setId(ID);
		jpa.setReference(" uco-1 ");
		jpa.setFine(ID);
		jpa.setPayer(ID);
		jpa.setInstitution(ID);
		jpa.setAmountInCents(-1L);
		jpa.setCurrency(" COP ");
		jpa.setExpiresAt(NOW);
		jpa.setCreatedAt(NOW);
		entity.setInstitution(null);
		entity.setCurrency(null);

		assertEquals(ID, jpa.getId());
		assertEquals("uco-1", jpa.getReference());
		assertEquals(ID, jpa.getFine());
		assertEquals(ID, jpa.getPayer());
		assertEquals(ID, jpa.getInstitution());
		assertEquals(0L, jpa.getAmountInCents());
		assertEquals("COP", jpa.getCurrency());
		assertEquals(NOW, jpa.getExpiresAt());
		assertEquals(NOW, jpa.getCreatedAt());
		assertEquals(UUIDHelper.getDefaultValue(), entity.getInstitution());
		assertEquals("", entity.getCurrency());
	}

	@Test
	@DisplayName("cada paso del pago guarda su transacción, estado, valor y origen")
	void eventStoresItsData() {
		final FinePaymentEventJpaEntity jpa = new FinePaymentEventJpaEntity();
		final FinePaymentEventEntity entity = new FinePaymentEventEntity();
		assertEquals("", jpa.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), entity.getReceivedAt());

		jpa.setId(ID);
		jpa.setPayment(ID);
		jpa.setTransaction(" t-1 ");
		jpa.setStatus(" APPROVED ");
		jpa.setAmountInCents(300000L);
		jpa.setSource(" EVENTO ");
		jpa.setReceivedAt(NOW);
		entity.setReceivedAt(NOW);
		entity.setSource(null);

		assertEquals(ID, jpa.getId());
		assertEquals(ID, jpa.getPayment());
		assertEquals("t-1", jpa.getTransaction());
		assertEquals("APPROVED", jpa.getStatus());
		assertEquals(300000L, jpa.getAmountInCents());
		assertEquals("EVENTO", jpa.getSource());
		assertEquals(NOW, jpa.getReceivedAt());
		assertEquals(NOW, entity.getReceivedAt());
		assertEquals("", entity.getSource());
	}

}
