package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.FineSettlementJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineSettlementJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineSettlementCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineSettlementEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class FineSettlementRepositoryAdapterTest {

	private static final UUID FINE = UUID.randomUUID();

	@Test
	@DisplayName("salda la multa cuando la base inserta la fila")
	void settlesWhenRowIsInserted() {
		final FakeJpaRepository<FineSettlementJpaEntity> repository = FakeJpaRepository.storing();

		assertTrue(adapter(repository).settle(settlement()));
		assertTrue(repository.filters().get(0).startsWith("settle ["));
	}

	@Test
	@DisplayName("no salda otra vez una multa que ya tiene su pago")
	void doesNotSettleTwice() {
		final FakeJpaRepository<FineSettlementJpaEntity> repository = FakeJpaRepository.<FineSettlementJpaEntity>storing()
				.changingRows(0);

		assertFalse(adapter(repository).settle(settlement()));
	}

	@Test
	@DisplayName("sin multa o sin referencia es un error inesperado")
	void rejectsIncompleteSettlement() {
		final FineSettlementRepositoryAdapter adapter = adapter(FakeJpaRepository.storing());
		final FineSettlementEntity incomplete = new FineSettlementEntity(null, null, null, null, null);

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> adapter.settle(incomplete)).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> adapter.settle(null)).getKind());
	}

	@Test
	@DisplayName("busca el pago con que quedó saldada una multa")
	void findsSettlementOfFine() {
		final FineSettlementJpaEntity stored = new FineSettlementJpaEntity();
		stored.setFine(FINE);
		stored.setPaymentReference("uco-1");
		final FakeJpaRepository<FineSettlementJpaEntity> repository = FakeJpaRepository.storing(stored);

		final List<FineSettlementEntity> found = adapter(repository).findByCriteria(FineSettlementCriteria.ofFine(FINE));

		assertEquals("uco-1", found.get(0).paymentReference());
		assertEquals(List.of("fine=" + FINE), repository.filters());
	}

	private static FineSettlementEntity settlement() {
		return new FineSettlementEntity(UUID.randomUUID(), FINE, "uco-1", new BigDecimal("3000"), null);
	}

	private static FineSettlementRepositoryAdapter adapter(final FakeJpaRepository<FineSettlementJpaEntity> repository) {
		return new FineSettlementRepositoryAdapter(repository.as(FineSettlementJpaRepository.class),
				new FakeEntityMapper<>(FineSettlementRepositoryAdapterTest::toEntity,
						FineSettlementRepositoryAdapterTest::toJpaEntity));
	}

	private static FineSettlementEntity toEntity(final FineSettlementJpaEntity jpaEntity) {
		return new FineSettlementEntity(jpaEntity.getId(), jpaEntity.getFine(), jpaEntity.getPaymentReference(),
				jpaEntity.getAmount(), jpaEntity.getSettledAt());
	}

	private static FineSettlementJpaEntity toJpaEntity(final FineSettlementEntity entity) {
		final FineSettlementJpaEntity jpaEntity = new FineSettlementJpaEntity();
		jpaEntity.setId(entity.id());
		jpaEntity.setFine(entity.fine());
		jpaEntity.setPaymentReference(entity.paymentReference());
		jpaEntity.setAmount(entity.amount());
		jpaEntity.setSettledAt(entity.settledAt());
		return jpaEntity;
	}

}
