package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.FineJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.testdata.FakeEntityMapper;
import co.com.bookhive.circulation.testdata.FakeJpaRepository;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class FineRepositoryAdapterTest {

	private static final UUID FINE = UUIDHelper.generateNew();
	private static final UUID USER = UUIDHelper.generateNew();

	@Test
	@DisplayName("filtra las multas pendientes del usuario")
	void findsPendingFinesOfUser() {
		final FakeJpaRepository<FineJpaEntity> repository = FakeJpaRepository.storing(jpaFine());

		final List<FineEntity> fines = adapter(repository).findByCriteria(FineCriteria.pendingByUser(USER));

		assertEquals(1, fines.size());
		assertEquals(FINE, fines.get(0).getId());
		assertEquals(List.of("user.id=" + USER, "status=" + FineEntity.STATUS_PENDING), repository.filters());
	}

	@Test
	@DisplayName("guarda la multa generada en la devolución")
	void createsFine() {
		final FakeJpaRepository<FineJpaEntity> repository = FakeJpaRepository.storing();
		final FineEntity fine = new FineEntity();
		fine.setId(FINE);

		adapter(repository).create(fine);

		assertEquals(FINE, repository.saved().get(0).getId());
	}

	@Test
	@DisplayName("sin usuario solo filtra por estado")
	void filtersOnlyByStatusWithoutUser() {
		final FakeJpaRepository<FineJpaEntity> repository = FakeJpaRepository.storing();

		adapter(repository).findByCriteria(FineCriteria.pendingByUser(null));

		assertEquals(List.of("status=" + FineEntity.STATUS_PENDING), repository.filters());
	}

	@Test
	@DisplayName("busca la multa de un préstamo")
	void findsFineOfLoan() {
		final FakeJpaRepository<FineJpaEntity> repository = FakeJpaRepository.storing();
		final UUID loan = UUID.randomUUID();

		adapter(repository).findByCriteria(FineCriteria.ofLoan(loan));

		assertEquals(List.of("loan.id=" + loan), repository.filters());
	}

	private static FineRepositoryAdapter adapter(final FakeJpaRepository<FineJpaEntity> repository) {
		return new FineRepositoryAdapter(repository.as(FineJpaRepository.class),
				new FakeEntityMapper<>(FineRepositoryAdapterTest::toEntity, FineRepositoryAdapterTest::toJpaEntity));
	}

	private static FineJpaEntity jpaFine() {
		final FineJpaEntity jpaEntity = new FineJpaEntity();
		jpaEntity.setId(FINE);
		return jpaEntity;
	}

	private static FineEntity toEntity(final FineJpaEntity jpaEntity) {
		final FineEntity entity = new FineEntity();
		entity.setId(jpaEntity.getId());
		return entity;
	}

	private static FineJpaEntity toJpaEntity(final FineEntity entity) {
		final FineJpaEntity jpaEntity = new FineJpaEntity();
		jpaEntity.setId(entity.getId());
		return jpaEntity;
	}

}
