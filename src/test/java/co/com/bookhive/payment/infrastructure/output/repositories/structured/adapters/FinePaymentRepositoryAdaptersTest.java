package co.com.bookhive.payment.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.FinePaymentEventJpaRepository;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.FinePaymentJpaRepository;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentEventJpaEntity;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentJpaEntity;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.payment.services.output.ports.repositories.criteria.FinePaymentCriteria;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEventEntity;

class FinePaymentRepositoryAdaptersTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID PAYER = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

	@Test
	@DisplayName("guarda el pago y lo busca por su referencia")
	void createsAndFindsByReference() {
		final FinePaymentJpaEntity stored = new FinePaymentJpaEntity();
		stored.setReference("uco-1");
		final FakeJpaRepository<FinePaymentJpaEntity> repository = FakeJpaRepository.storing(stored);
		final FinePaymentRepositoryAdapter adapter = paymentAdapter(repository);
		final FinePaymentEntity payment = new FinePaymentEntity();
		payment.setReference("uco-2");

		adapter.create(payment);
		final List<FinePaymentEntity> found = adapter.findByCriteria(FinePaymentCriteria.withReference("uco-1"));

		assertEquals("uco-2", repository.saved().get(0).getReference());
		assertEquals("uco-1", found.get(0).getReference());
		assertEquals(List.of("reference=uco-1"), repository.filters());
	}

	@Test
	@DisplayName("los pagos abiertos de una multa son los vigentes sin respuesta definitiva")
	void findsOpenPayments() {
		final FakeJpaRepository<FinePaymentJpaEntity> repository = FakeJpaRepository.storing();

		paymentAdapter(repository).findByCriteria(FinePaymentCriteria.openFor(FINE, PAYER, NOW));

		assertEquals("fine=" + FINE, repository.filters().get(0));
		assertEquals("payer=" + PAYER, repository.filters().get(1));
		assertEquals("expiresAt>" + NOW, repository.filters().get(2));
		assertTrue(repository.filters().contains("existe la subconsulta"));
		assertEquals("negada", repository.filters().get(repository.filters().size() - 1));
	}

	@Test
	@DisplayName("la revisión busca los pagos sin respuesta creados en la ventana")
	void findsUnresolvedPaymentsCreatedBetween() {
		final FakeJpaRepository<FinePaymentJpaEntity> repository = FakeJpaRepository.storing();

		paymentAdapter(repository).findByCriteria(
				FinePaymentCriteria.unresolvedCreatedBetween(NOW.minusSeconds(60), NOW));

		assertEquals("createdAt entre " + NOW.minusSeconds(60) + " y " + NOW, repository.filters().get(0));
	}

	@Test
	@DisplayName("sin ningún filtro no devuelve todos los pagos")
	void withoutFiltersFindsNothing() {
		final FakeJpaRepository<FinePaymentJpaEntity> repository = FakeJpaRepository.storing();

		paymentAdapter(repository).findByCriteria(null);

		assertEquals(List.of("ninguna"), repository.filters());
	}

	@Test
	@DisplayName("cada paso del pago se guarda una sola vez")
	void recordsEachStepOnce() {
		final FakeJpaRepository<FinePaymentEventJpaEntity> first = FakeJpaRepository.storing();
		final FakeJpaRepository<FinePaymentEventJpaEntity> repeated =
				FakeJpaRepository.<FinePaymentEventJpaEntity>storing().changingRows(0);

		assertTrue(eventAdapter(first).register(event()));
		assertFalse(eventAdapter(repeated).register(event()));
		assertTrue(first.filters().get(0).startsWith("register ["));
	}

	@Test
	@DisplayName("un paso sin transacción o sin estado es un error inesperado")
	void rejectsIncompleteStep() {
		final FinePaymentEventRepositoryAdapter adapter = eventAdapter(FakeJpaRepository.storing());
		final FinePaymentEventEntity incomplete = new FinePaymentEventEntity();

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> adapter.register(incomplete)).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> adapter.register(null)).getKind());
	}

	private static FinePaymentEventEntity event() {
		final FinePaymentEventEntity event = new FinePaymentEventEntity();
		event.setId(UUID.randomUUID());
		event.setPayment(UUID.randomUUID());
		event.setTransaction("t-1");
		event.setStatus("APPROVED");
		event.setAmountInCents(300000L);
		event.setSource("EVENTO");
		return event;
	}

	private static FinePaymentRepositoryAdapter paymentAdapter(final FakeJpaRepository<FinePaymentJpaEntity> repository) {
		return new FinePaymentRepositoryAdapter(repository.as(FinePaymentJpaRepository.class), new PaymentMapper());
	}

	private static FinePaymentEventRepositoryAdapter eventAdapter(
			final FakeJpaRepository<FinePaymentEventJpaEntity> repository) {
		return new FinePaymentEventRepositoryAdapter(repository.as(FinePaymentEventJpaRepository.class));
	}

	private static final class PaymentMapper implements EntityMapper<FinePaymentEntity, FinePaymentJpaEntity> {

		@Override
		public FinePaymentEntity toEntity(final FinePaymentJpaEntity jpaEntity) {
			final FinePaymentEntity entity = new FinePaymentEntity();
			entity.setReference(jpaEntity.getReference());
			return entity;
		}

		@Override
		public FinePaymentJpaEntity toJpaEntity(final FinePaymentEntity entity) {
			final FinePaymentJpaEntity jpaEntity = new FinePaymentJpaEntity();
			jpaEntity.setReference(entity.getReference());
			return jpaEntity;
		}

	}

}
